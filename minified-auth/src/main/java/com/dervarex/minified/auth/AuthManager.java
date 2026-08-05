package com.dervarex.minified.auth;

import com.dervarex.minified.auth.encryption.Encryptor;
import com.dervarex.minified.auth.events.LoginStateChangeListener;
import com.dervarex.minified.auth.exceptions.LoginFailedException;
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

import javax.crypto.SecretKey;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused") // required to make intelliJ shut up
public class AuthManager {

    private static final Map<String, User> session = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static final List<LoginStateChangeListener> listeners = new CopyOnWriteArrayList<>();
    private static Path BASE_DIR;
    private static Path KEY_FILE;
    private static Path SESSION_FILE;
    private static EventBus eventBus;
    private static SecretKey masterKey;
    private static volatile LoginState loginState = new LoginState();
    private static volatile CountDownLatch codeReadyLatch = null;

    /**
     * Initializes the auth manager using the given directory.
     *
     * @param BaseDir  the directory where the key and session files will be stored.
     * @param eventBus the event bus to push event updates to
     */

    public static void init(Path BaseDir, EventBus eventBus) {
        BASE_DIR = BaseDir;
        SESSION_FILE = BASE_DIR.resolve("session.enc");
        KEY_FILE = BASE_DIR.resolve("master.key");
        prepareKeyDirectories();
        JavaManager.init(BASE_DIR.resolve("java"));
    }

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
    public static void init(String launcherName, EventBus eventBus) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            BASE_DIR = Path.of(System.getenv("APPDATA")); // windows
        } else if (os.contains("mac")) {
            BASE_DIR = Path.of(System.getProperty("user.home"), "Library", "Application Support"); // macos
        } else {
            BASE_DIR = Path.of(System.getProperty("user.home"), ".local", "share"); // linux
        }
        SESSION_FILE = BASE_DIR.resolve("session.enc");
        KEY_FILE = BASE_DIR.resolve("master.key");
        prepareKeyDirectories();
        JavaManager.init(BASE_DIR.resolve("java"));
    }

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
     *     LoginState state = AuthManager.getLoginState();
     *     if (state.userCode != null && state.verificationUri != null) {
     *         System.out.println(state.verificationUri + " -> " + state.userCode);
     *         break;
     *     }
     *     try { Thread.sleep(100); } catch (InterruptedException ignored) {}
     * }
     * }</pre>
     */

    public static User login() {
        HttpClient httpClient = MinecraftAuth.createHttpClient();
        try {
            NetworkUtil.ensureOnline("DeviceCodeLogin");
            StepFullJavaSession.FullJavaSession javaSession =
                    MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(httpClient,
                            new StepMsaDeviceCode.MsaDeviceCodeCallback(msa -> {
                                // expose code & urls immediately
                                loginState.userCode = msa.getUserCode();
                                loginState.verificationUri = msa.getVerificationUri();
                                loginState.directVerificationUri = msa.getDirectVerificationUri();
                                loginState.status = LoginStatus.PENDING;
                                loginState.message = "Waiting for user to authorize in browser";
                                System.out.println("Go to " + msa.getVerificationUri());
                                System.out.println("Enter code " + msa.getUserCode());
                                System.out.println("Direct URL: " + msa.getDirectVerificationUri());
                            }));

            User user = persistSession(javaSession);
            System.out.println("Login successful for " + user.username());
            return user;
        } catch (NoConnectionException nce) {
            System.out.println(nce.getMessage());
            throw new RuntimeException(nce.toUserFriendlyMessage(), nce);
        } catch (Exception e) {
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
        Encryptor.saveEncryptedSession(serialized, masterKey, SESSION_FILE, eventBus);

        StepMCProfile.MCProfile profile = javaSession.getMcProfile();
        User user = new User(profile.getId().toString(),
                profile.getName(),
                profile.getMcToken().getAccessToken(),
                serialized);
        session.put(user.uuid(), user);
        return user;
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
    public static synchronized String startDeviceCodeLoginAsync() {
        // Already in progress?
        if (loginState.status == LoginStatus.PENDING || loginState.status == LoginStatus.STARTING) {
            System.out.println("Login already in progress");
            return GSON.toJson(loginState);
        }
        loginState = new LoginState();
        loginState.status = LoginStatus.STARTING;
        loginState.message = "Starting device code login";
        System.out.println("Starting device code login");
        codeReadyLatch = new CountDownLatch(1);

        new Thread(() -> {
            HttpClient httpClient = MinecraftAuth.createHttpClient();
            try {
                NetworkUtil.ensureOnline("DeviceCodeLoginAsync");
                StepFullJavaSession.FullJavaSession javaSession = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(
                        httpClient,
                        new StepMsaDeviceCode.MsaDeviceCodeCallback(msa -> {
                            // expose code & urls immediately
                            loginState.userCode = msa.getUserCode();
                            loginState.verificationUri = msa.getVerificationUri();
                            loginState.directVerificationUri = msa.getDirectVerificationUri();
                            loginState.status = LoginStatus.PENDING;
                            loginState.message = "Waiting for user to authorize in browser";
                            System.out.println("Waiting for user authorization");
                            //try { OSUtil.openBrowser(msa.getDirectVerificationUri()); } catch (Exception ignored) {}
                            if (codeReadyLatch != null) codeReadyLatch.countDown();
                        })
                );

                User user = persistSession(javaSession);

                loginState.status = LoginStatus.SUCCESS;
                loginState.username = user.username();
                loginState.message = "Login successful";
                System.out.println("Login successful for " + user.username());
                if (codeReadyLatch != null) codeReadyLatch.countDown();
            } catch (NoConnectionException nce) {
                loginState.status = LoginStatus.ERROR;
                loginState.message = nce.getMessage();
                System.out.println("Connectivity error: " + nce.getMessage());
                if (codeReadyLatch != null) codeReadyLatch.countDown();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                loginState.status = LoginStatus.ERROR;
                loginState.message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                System.out.println("Async login failed: " + loginState.message + "\n" + sw);
                if (codeReadyLatch != null) codeReadyLatch.countDown();
            }
        }, "DeviceCodeLoginThread").start();

        try {
            if (codeReadyLatch != null) codeReadyLatch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        return GSON.toJson(loginState);
    }

    /**
     * @return The current login state as a JSON string, which can be used for UI display or debugging purposes.
     */

    public static synchronized String getLoginStateJson() {
        System.out.println("Login state requested");
        return GSON.toJson(loginState);
    }

    /**
     * @return the current {@code LoginState}, can be used for displaying purposes.
     * <p>
     * Please note that updating the state is currently only done internally and not guaranteed to be thread-safe,
     * so this should be used for display purposes only and not for any critical logic.
     */
    public static synchronized LoginState getLoginState() {
        return loginState;
    }

    /**
     * Refreshes the login state.
     */
    public static synchronized void resetLoginState() {
        loginState = new LoginState();
        codeReadyLatch = null;
        System.out.println("Login state reset");
    }

    /**
     * Attempts to load a saved session from disk, refreshes it if possible, and returns the corresponding User.
     *
     * @return the logged-in user, or nul if no valid session could be found.
     */

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
     * @return if there is a saved session on disk(uses the directories given in {@code init(BaseDir)}
     * <p>
     * Note that this does not check if the session is still valid.
     */
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
    public static User getUser() {
        return session.values().stream().findFirst().orElse(null);
    }

    public static void addStateChangeListener(LoginStateChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeStateChangeListener(LoginStateChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyStateChanged() {
        for (LoginStateChangeListener listener : listeners) {
            listener.onStateChanged(loginState);
        }
    }

    // Async login state management
    public enum LoginStatus {IDLE, STARTING, PENDING, SUCCESS, ERROR}
}
