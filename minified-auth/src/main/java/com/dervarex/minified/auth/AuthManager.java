package com.dervarex.minified.auth;

import com.dervarex.minified.auth.encryption.Encryptor;
import com.dervarex.minified.auth.events.LoginStateChangeListener;
import com.dervarex.minified.auth.exceptions.LoginFailedException;
import com.dervarex.minified.auth.user.User;
import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.java.JavaManager;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.network.NetworkUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.StepMCProfile;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import org.apiguardian.api.API;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class AuthManager {

    private static final Map<String, User> session = new ConcurrentHashMap<>();
    private static final Gson GSON = new Gson();
    private static final List<LoginStateChangeListener> listeners = new CopyOnWriteArrayList<>();
    private static Path BASE_DIR;
    private static Path KEY_FILE;
    private static Path SESSION_FILE;
    private static Path SESSIONS_DIR;
    private static EventBus eventBus;
    private static SecretKey masterKey;
    private static volatile LoginState loginState = new LoginState();
    private static volatile CountDownLatch codeReadyLatch = null;

    /**
     * Initializes the auth manager using the given directory.
     *
     * @param baseDir  the directory where the key and session files will be stored.
     * @param eventBus the event bus to push event updates to
     */
    @API(status = API.Status.STABLE)
    public static void init(Path baseDir, EventBus eventBus) {
        AuthManager.eventBus = eventBus;
        BASE_DIR = baseDir;
        SESSION_FILE = BASE_DIR.resolve("session.enc");
        SESSIONS_DIR = BASE_DIR.resolve("sessions");
        KEY_FILE = BASE_DIR.resolve("master.key");
        prepareKeyDirectories();
        JavaManager.init(BASE_DIR.resolve("java"));
    }
    @API(status = API.Status.STABLE)
    public static void init(Path BaseDir) {
        init(BaseDir, new EventBus());
    }

    /**
     * Initializes the auth manager using the default application data directory.
     * <p>
     * It is recommended to use {@code init(Path baseDir)} instead
     * if you want full control over the storage location.
     *
     * @param launcherName the launcher name used to create the application directory
     * @param eventBus     the event bus to push event updates to
     */
    @API(status = API.Status.STABLE)
    public static void init(String launcherName, EventBus eventBus) {
        AuthManager.eventBus = eventBus;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            BASE_DIR = Path.of(System.getenv("APPDATA")); // windows
        } else if (os.contains("mac")) {
            BASE_DIR = Path.of(System.getProperty("user.home"), "Library", "Application Support"); // macos
        } else {
            BASE_DIR = Path.of(System.getProperty("user.home"), ".local", "share"); // linux
        }
        BASE_DIR = BASE_DIR.resolve(launcherName);
        SESSION_FILE = BASE_DIR.resolve("session.enc");
        SESSIONS_DIR = BASE_DIR.resolve("sessions");
        KEY_FILE = BASE_DIR.resolve("master.key");
        prepareKeyDirectories();
        JavaManager.init(BASE_DIR.resolve("java"));
    }
    @API(status = API.Status.STABLE)
    public static void init(String launcherName) {
        init(launcherName, new EventBus());
    }

    private static void prepareKeyDirectories() {
        try {
            if (!Files.exists(BASE_DIR)) Files.createDirectories(BASE_DIR);
            System.out.println("Auth base dir ready at " + BASE_DIR);
            masterKey = Encryptor.loadOrCreateMasterKey(KEY_FILE, eventBus);
            System.out.println("AuthManager initialized");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println("AuthManager init failed: " + sw);
        }
    }

    /**
     * Starts the device-code login flow and blocks until the login completes.
     * <p>
     * While this method is running, the device verification URL and user code are published to
     * the shared {@link #loginState} as soon as they are issued by the provider. This allows a
     * UI thread to call {@link #getLoginState()} (or {@link #getLoginStateJson()}) and display
     * the URL/code before {@code login()} returns.
     * <p>
     * Typical use: call {@link #startDeviceCodeLoginAsync()} from UI code and poll the state.
     * Use this blocking method only in special cases where a background thread is guaranteed,
     * and you explicitly want a synchronous result.
     * <p>
     * Example:
     * <pre>{@code
     * new Thread(AuthManager::login).start();
     * while (true) {
     * LoginState state = AuthManager.getLoginState();
     * if (state.userCode != null && state.verificationUri != null) {
     * System.out.println(state.verificationUri + " -> " + state.userCode);
     * break;
     * }
     * try { Thread.sleep(100); } catch (InterruptedException ignored) {}
     * }
     * }</pre>
     */
    @API(status = API.Status.STABLE)
    public static User login() {
        HttpClient httpClient = MinecraftAuth.createHttpClient();
        try {
            NetworkUtil.ensureOnline("DeviceCodeLogin");
            StepFullJavaSession.FullJavaSession javaSession =
                    MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(httpClient,
                            new StepMsaDeviceCode.MsaDeviceCodeCallback(msa -> {
                                // expose code & URLs immediately
                                loginState.userCode = msa.getUserCode();
                                loginState.verificationUri = msa.getVerificationUri();
                                loginState.directVerificationUri = msa.getDirectVerificationUri();
                                loginState.status = LoginStatus.PENDING;
                                loginState.message = "Waiting for user to authorize in browser";
                                notifyStateChanged();
                                System.out.println("Go to " + msa.getVerificationUri());
                                System.out.println("Enter code " + msa.getUserCode());
                                System.out.println("Direct URL: " + msa.getDirectVerificationUri());
                            }));

            User user = persistSession(javaSession);
            loginState.status = LoginStatus.SUCCESS;
            loginState.username = user.username();
            loginState.message = "Login successful";
            notifyStateChanged();
            System.out.println("Login successful for " + user.username());
            return user;
        } catch (NoConnectionException nce) {
            loginState.status = LoginStatus.ERROR;
            loginState.message = nce.getMessage();
            notifyStateChanged();
            System.out.println(nce.getMessage());
            throw new RuntimeException(nce.toUserFriendlyMessage(), nce);
        } catch (Exception e) {
            loginState.status = LoginStatus.ERROR;
            loginState.message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            notifyStateChanged();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println("Login failed: " + sw);
            throw new LoginFailedException("Login failed", e);
        }
    }

    /**
     * Serializes, encrypts and stores the session,
     * <p>
     * then creates and caches the corresponding User instance.
     */
    private static User persistSession(StepFullJavaSession.FullJavaSession javaSession) throws Exception {
        JsonObject serialized = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.toJson(javaSession);
        // session.enc is the most recent login, so we don't break the backwards compatibility of v3.2.0 and before
        Encryptor.saveEncryptedSession(serialized, masterKey, SESSION_FILE, eventBus);

        User user = toUser(javaSession, serialized);
        Files.createDirectories(SESSIONS_DIR);
        Encryptor.saveEncryptedSession(serialized, masterKey, sessionFile(user.getMinecraftUUID().getDashed()), eventBus);
        session.put(user.getMinecraftUUID().getDashed().toString(), user);
        return user;
    }

    private static User toUser(StepFullJavaSession.FullJavaSession javaSession, JsonObject serialized) {
        StepMCProfile.MCProfile profile = javaSession.getMcProfile();
        return new User(profile.getId(),
                profile.getName(),
                profile.getMcToken().getAccessToken(),
                serialized);
    }

    private static Path sessionFile(UUID uuid) {
        return SESSIONS_DIR.resolve(uuid.toString().replace("-", "") + ".enc");
    }

    /**
     * Starts the device-code login flow on a background thread and returns immediately.
     * <p>
     * Typical use: call this from UI code, then poll {@link #getLoginState()} (or JSON) to
     * show the verification URL and user code while the login is pending.
     * Use {@link #login()} only in special cases where a synchronous, blocking call on a
     * dedicated background thread is preferred.
     *
     * @return the initial login state as JSON, usually {@code STARTING} or {@code PENDING}
     */
    @API(status = API.Status.EXPERIMENTAL)
    public static String startDeviceCodeLoginAsync() {
        final LoginState currentState;
        final CountDownLatch latch;

        synchronized (AuthManager.class) {
            // Already in progress?
            if (loginState.status == LoginStatus.PENDING || loginState.status == LoginStatus.STARTING) {
                System.out.println("Login already in progress");
                return GSON.toJson(loginState);
            }
            currentState = new LoginState();
            currentState.status = LoginStatus.STARTING;
            currentState.message = "Starting device code login";
            loginState = currentState;
            codeReadyLatch = new CountDownLatch(1);
            latch = codeReadyLatch;
        }
        notifyStateChanged(currentState);
        System.out.println("Starting device code login");

        new Thread(() -> {
            HttpClient httpClient = MinecraftAuth.createHttpClient();
            try {
                NetworkUtil.ensureOnline("DeviceCodeLoginAsync");
                StepFullJavaSession.FullJavaSession javaSession = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(
                        httpClient,
                        new StepMsaDeviceCode.MsaDeviceCodeCallback(msa -> {
                            // expose code & URLs immediately
                            currentState.userCode = msa.getUserCode();
                            currentState.verificationUri = msa.getVerificationUri();
                            currentState.directVerificationUri = msa.getDirectVerificationUri();
                            currentState.status = LoginStatus.PENDING;
                            currentState.message = "Waiting for user to authorize in browser";
                            notifyStateChanged(currentState);
                            System.out.println("Waiting for user authorization");
                            //try { OSUtil.openBrowser(msa.getDirectVerificationUri()); } catch (Exception ignored) {}
                            latch.countDown();
                        })
                );

                User user = persistSession(javaSession);

                currentState.status = LoginStatus.SUCCESS;
                currentState.username = user.username();
                currentState.message = "Login successful";
                notifyStateChanged(currentState);
                System.out.println("Login successful for " + user.username());
                latch.countDown();
            } catch (NoConnectionException nce) {
                currentState.status = LoginStatus.ERROR;
                currentState.message = nce.getMessage();
                notifyStateChanged(currentState);
                System.out.println("Connectivity error: " + nce.getMessage());
                latch.countDown();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                currentState.status = LoginStatus.ERROR;
                currentState.message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                notifyStateChanged(currentState);
                System.out.println("Async login failed: " + currentState.message + "\n" + sw);
                latch.countDown();
            }
        }, "DeviceCodeLoginThread").start();

        try {
            boolean ready = latch.await(2, TimeUnit.SECONDS);
            if (!ready) {
                System.out.println("Timed out waiting for code or login result");
            }
        } catch (InterruptedException ignored) {
        }
        return GSON.toJson(loginState);
    }

    /**
     * @return The current login state as a JSON string, which can be used for UI display or debugging purposes.
     */
    @API(status=API.Status.STABLE)
    public static synchronized String getLoginStateJson() {
        System.out.println("Login state requested");
        return GSON.toJson(loginState);
    }

    /**
     * Returns a thread safe snapshot of the current {@link LoginState}.
     * <p>
     * Safe to be called from any thread (e.g. UI polling loops).
     *
     * @return a copy of the current login state.
     */
    @API(status = API.Status.STABLE)
    public static synchronized LoginState getLoginState() {
        return new LoginState(loginState);
    }

    /**
     * Refreshes the login state.
     */
    @API(status = API.Status.STABLE)
    public static synchronized void resetLoginState() {
        loginState = new LoginState();
        codeReadyLatch = null;
        notifyStateChanged();
        System.out.println("Login state reset");
    }

    /**
     * Attempts to load a saved session from disk, refreshes it if possible, and returns the corresponding User.
     *
     * @return the logged-in user, or nul if no valid session could be found.
     */
    @API(status = API.Status.STABLE)
    public static User loginWithSavedSession() {
        System.out.println("Login with saved session");
        try {
            JsonObject saved = Encryptor.loadEncryptedSession(SESSION_FILE, masterKey, eventBus);
            if (saved == null) return null;

            HttpClient httpClient = MinecraftAuth.createHttpClient();

            StepFullJavaSession.FullJavaSession loaded =
                    MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.fromJson(saved);

            StepFullJavaSession.FullJavaSession refreshed =
                    MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.refresh(httpClient, loaded);

            // Refresh saved token if it has changed
            User user = persistSession(refreshed);
            System.out.println("Saved session OK for " + user.username());
            return user;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println("Login/Refresh failed: " + sw);
            return null;
        }
    }

    /**
     * Lists every account that has a saved session. Every successful login saves one, next to the
     * single session used by {@link #loginWithSavedSession()}
     *
     * @return the UUIDs of all accounts with a saved session, no order
     * @throws IOException if the session directory exists but can't be read
     */
    @API(status = API.Status.EXPERIMENTAL, since = "v3.2.1")
    public static List<UUID> getSavedAccounts() throws IOException {
        List<UUID> accounts = new ArrayList<>();
        if (!Files.isDirectory(SESSIONS_DIR)) return accounts;
        try (Stream<Path> files = Files.list(SESSIONS_DIR)) {
            for (Path file : files.toList()) {
                String name = file.getFileName().toString();
                if (!name.endsWith(".enc")) continue;
                accounts.add(parseUndashed(name.substring(0, name.length() - ".enc".length())));
            }
        }
        return accounts;
    }

    /**
     * Loads the saved session of one account and refreshes it, like {@link #loginWithSavedSession()} does
     * for the most recent one
     *
     * @param uuid the account to log in with
     * @return the logged in user, or null if there is no saved session for it or it could not be refreshed
     */
    @API(status = API.Status.EXPERIMENTAL, since = "v3.2.1")
    public static User loginWithSavedSession(UUID uuid) {
        try {
            JsonObject saved = Encryptor.loadEncryptedSession(sessionFile(uuid), masterKey, eventBus);
            if (saved == null) return null;

            HttpClient httpClient = MinecraftAuth.createHttpClient();
            StepFullJavaSession.FullJavaSession refreshed = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.refresh(
                    httpClient,
                    MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.fromJson(saved)
            );
            User user = persistSession(refreshed);
            System.out.println("Saved session OK for " + user.username());
            return user;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println("Login/Refresh failed for " + uuid + ": " + sw);
            return null;
        }
    }

    /**
     * Loads the saved session of one account without refreshing it, for when there is no network
     * The access token may have expired, that's fine for singleplayer but online servers will refuse it
     * Will not work if disk is on fire, though we're already working on a fire extinguisher
     *
     * @param uuid the account to load
     * @return the user from the saved session, or null if there is none
     * @throws LoginFailedException if the session file exists but can't be decrypted or read
     */
    @API(status = API.Status.EXPERIMENTAL, since = "v3.2.1")
    public static User loadSavedSession(UUID uuid) {
        try {
            JsonObject saved = Encryptor.loadEncryptedSession(sessionFile(uuid), masterKey, eventBus);
            if (saved == null) return null;
            User user = toUser(MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.fromJson(saved), saved);
            session.put(user.getMinecraftUUID().getDashed().toString(), user);
            return user;
        } catch (Exception e) {
            throw new LoginFailedException("Failed to read the saved session of " + uuid, e);
        }
    }

    /**
     * Forgets an account: deletes its saved session, and {@code session.enc} too if that one belongs to it
     *
     * @param uuid the account to remove
     * @return true if a saved session was deleted
     * @throws IOException if a session file can't be deleted
     */
    @API(status = API.Status.EXPERIMENTAL, since = "v3.2.1")
    public static boolean removeSavedSession(UUID uuid) throws IOException {
        session.remove(uuid.toString());
        boolean removed = Files.deleteIfExists(sessionFile(uuid));
        if (Files.exists(SESSION_FILE)) {
            try {
                JsonObject latest = Encryptor.loadEncryptedSession(SESSION_FILE, masterKey, eventBus);
                if (latest != null && uuid.equals(MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.fromJson(latest).getMcProfile().getId())) {
                    removed |= Files.deleteIfExists(SESSION_FILE);
                }
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException("Failed to read " + SESSION_FILE, e);
            }
        }
        return removed;
    }

    private static UUID parseUndashed(String undashed) {
        return UUID.fromString(undashed.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"));
    }

    /**
     * @return if there is a saved session on disk(uses the directories given in {@code init(BaseDir)}
     * <p>
     * Note that this does not check if the session is still valid.
     */
    @API(status = API.Status.STABLE)
    public static boolean hasSessionSaved() {
        boolean exists = SESSION_FILE.toFile().exists();
        System.out.println("Has saved session: " + exists);
        return exists;
    }

    /**
     * @return the currently logged-in user, or null if no session is active.
     * <p>
     * Note that this does not check if the session is still valid.
     */
    @API(status = API.Status.STABLE)
    public static User getUser() {
        return session.values().stream().findFirst().orElse(null);
    }

    @API(status = API.Status.STABLE)
    public static void addStateChangeListener(LoginStateChangeListener listener) {
        listeners.add(listener);
    }

    @API(status = API.Status.STABLE)
    public static void removeStateChangeListener(LoginStateChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyStateChanged() {
        notifyStateChanged(loginState);
    }

    private static void notifyStateChanged(LoginState state) {
        for (LoginStateChangeListener listener : listeners) {
            listener.onStateChanged(state);
        }
    }

    // Async login state management
    public enum LoginStatus {IDLE, STARTING, PENDING, SUCCESS, ERROR}
}