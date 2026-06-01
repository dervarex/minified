package com.dervarex.minified.launch;

import com.dervarex.minified.auth.AuthManager;
import com.dervarex.minified.auth.LoginState;
import com.dervarex.minified.auth.User;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LauncherTest {

    private static void printLaunchDiagnostics() {
        System.out.println("--- Launch diagnostics ---");
        System.out.println("os.name = " + System.getProperty("os.name"));
        System.out.println("os.version = " + System.getProperty("os.version"));
        System.out.println("java.version = " + System.getProperty("java.version"));
        System.out.println("java.vendor = " + System.getProperty("java.vendor"));
        System.out.println("XDG_RUNTIME_DIR = " + System.getenv("XDG_RUNTIME_DIR"));
        System.out.println("XDG_SESSION_TYPE = " + System.getenv("XDG_SESSION_TYPE"));
        System.out.println("WAYLAND_DISPLAY = " + System.getenv("WAYLAND_DISPLAY"));
        System.out.println("DISPLAY = " + System.getenv("DISPLAY"));
        System.out.println("XAUTHORITY = " + System.getenv("XAUTHORITY"));
        System.out.println("XDG_CURRENT_DESKTOP = " + System.getenv("XDG_CURRENT_DESKTOP"));
        System.out.println("GDK_BACKEND = " + System.getenv("GDK_BACKEND"));
        System.out.println("LIBGL_ALWAYS_SOFTWARE = " + System.getenv("LIBGL_ALWAYS_SOFTWARE"));
        printRunningProcessDiagnostics("xwayland-satellite");
        System.out.println("java.library.path = " + System.getProperty("java.library.path"));
        System.out.println("--------------------------");
    }

    private static void printRunningProcessDiagnostics(String needle) {
        boolean found = false;

        for (ProcessHandle process : ProcessHandle.allProcesses().toArray(ProcessHandle[]::new)) {
            ProcessHandle.Info info = process.info();
            String commandLine = info.commandLine().orElse("");
            String command = info.command().orElse("");

            if (!commandLine.contains(needle) && !command.contains(needle)) {
                continue;
            }

            found = true;
            System.out.println("Detected process matching '" + needle + "': pid=" + process.pid());
            if (!commandLine.isBlank()) {
                System.out.println("  commandLine = " + commandLine);
            } else if (!command.isBlank()) {
                System.out.println("  command = " + command);
            }
        }

        if (!found) {
            System.out.println("No running process matched '" + needle + "'.");
        }
    }

    public static void main(String[] args) throws Exception {

        printLaunchDiagnostics();

        AuthManager.init(Path.of("/home/dervarex/Development/tmp/login/"));

        User user = null;

        // try to login with saved session
        if (AuthManager.hasSessionSaved()) {
            try {
                user = AuthManager.loginWithSavedSession();

                if (user != null) {
                    System.out.println("Auto-login successful:");
                    System.out.println(user.getUsername() + " (" + user.getUuid() + ")");
                }

            } catch (Exception e) {
                System.out.println("Saved session invalid.");
                e.printStackTrace();
            }
        }

        // if theres no session saved, login with microsoft instead
        if (user == null) {

            System.out.println("Starting Microsoft login...");

            AuthManager.startDeviceCodeLoginAsync();

            long timeoutMs = 5L * 60L * 1000L;
            long start = System.currentTimeMillis();

            LoginState state;

            do {

                state = AuthManager.getLoginState();

                if (state.userCode != null && state.verificationUri != null) {
                    System.out.println(
                            "Go to " + state.verificationUri +
                                    " and enter code " + state.userCode
                    );
                }

                if (state.status == AuthManager.LoginStatus.SUCCESS ||
                        state.status == AuthManager.LoginStatus.ERROR) {
                    break;
                }

                Thread.sleep(500);

            } while (System.currentTimeMillis() - start < timeoutMs);

            state = AuthManager.getLoginState();

            assertEquals(
                    AuthManager.LoginStatus.SUCCESS,
                    state.status,
                    "Login failed"
            );

            user = AuthManager.getUser();

            assertNotNull(user);

            System.out.println("Logged in as:");
            System.out.println(user.getUsername() + " (" + user.getUuid() + ")");
        }
        LaunchConfigurator config = new LaunchConfigurator.Builder()
                .downloadThreads(10)
                .launcherName("MinifiedLauncher")
                .launcherVersion("1.0.0")
                .build();

        Launcher.launchMinecraft(
                "1.21.11",
                Path.of("/home/dervarex/Development/tmp/jar/client.jar"),
                Path.of("/home/dervarex/Development/tmp/libs/"),
                Path.of("/home/dervarex/Development/tmp/assets/"),
                2024,
                4048,
                user,
                config
        );
    }
}