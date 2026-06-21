package com.dervarex.minified.launch;

import com.dervarex.minified.auth.User;
import com.dervarex.minified.launch.launch.Launcher;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LaunchTest {

    public static void main(String[] args) {
        User user = AuthTest.auth();

        Launcher.launchMinecraft(
                "26.1.2",
                user,
                TestEnvironment.config()
        );
    }
}