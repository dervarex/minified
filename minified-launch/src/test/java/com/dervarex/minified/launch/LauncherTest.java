package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.LaunchConfigurator;
import com.dervarex.minified.launch.launch.Launcher;
import com.dervarex.minified.launch.launch.modding.Loader;

import java.nio.file.Path;

public class LauncherTest {

    public static void main(String[] args) {

        //printLaunchDiagnostics();

//        AuthManager.init(Path.of("/home/dervarex/Development/tmp/login/"));
//
//        User user = null;
//
//        // try to log in with saved session
//        if (AuthManager.hasSessionSaved()) {
//            try {
//                user = AuthManager.loginWithSavedSession();
//
//                if (user != null) {
//                    System.out.println("Auto-login successful:");
//                    System.out.println(user.getUsername() + " (" + user.getUuid() + ")");
//                }
//
//            } catch (Exception e) {
//                System.out.println("Saved session invalid.");
//                e.printStackTrace();
//            }
//        }
//
//        // if there's no session saved, login with Microsoft instead
//        if (user == null) {
//
//            System.out.println("Starting Microsoft login...");
//
//            AuthManager.startDeviceCodeLoginAsync();
//
//            long timeoutMs = 5L * 60L * 1000L;
//            long start = System.currentTimeMillis();
//
//            LoginState state;
//
//            do {
//
//                state = AuthManager.getLoginState();
//
//                if (state.userCode != null && state.verificationUri != null) {
//                    System.out.println(
//                            "Go to " + state.verificationUri +
//                                    " and enter code " + state.userCode
//                    );
//                }
//
//                if (state.status == AuthManager.LoginStatus.SUCCESS ||
//                        state.status == AuthManager.LoginStatus.ERROR) {
//                    break;
//                }
//
//                Thread.sleep(500);
//
//            } while (System.currentTimeMillis() - start < timeoutMs);
//
//            state = AuthManager.getLoginState();
//
//            assertEquals(
//                    AuthManager.LoginStatus.SUCCESS,
//                    state.status,
//                    "Login failed"
//            );
//
//            user = AuthManager.getUser();
//
//            assertNotNull(user);

//            System.out.println("Logged in as:");
//            System.out.println(user.getUsername() + " (" + user.getUuid() + ")");//
//            }
        LaunchConfigurator config = new LaunchConfigurator.Builder()
                .downloadThreads(10)
                .launcherName("MinifiedLauncher")
                .launcherVersion("1.0.0")
                .assetsDirectory(Path.of("/home/dervarex/Development/tmp/assets/"))
                .librariesDirectory(Path.of("/home/dervarex/Development/tmp/jar/libraries/"))
                .jarFile(Path.of("/home/dervarex/Development/tmp/jar/client.jar"))
                .isDemoUser(false)
                .loader(Loader.Vanilla)
                .build();


        Launcher.launchMinecraft(
                "26.1.2",
                //user,
                null,
                config
        );
    }
}