package com.dervarex.minified.launch;

import com.dervarex.minified.auth.User;
import com.dervarex.minified.launch.launch.Launcher;

public class LaunchTest {

    public static void main(String[] args) {
        User user = AuthTest.auth();

        Launcher.launchMinecraft(
                user,
                TestEnvironment.config()
        );
    }
}