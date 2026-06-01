package com.dervarex.minified.launch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class LaunchOptionsTest {

    @Test
    void setMethodsAreFluentAndPersistValues() {
        LaunchOptions options = LaunchOptions.create();

        LaunchOptions chained = options
                .setVariable("auth_player_name", "Steve")
                .setVariable("auth_access_token", "token")
                .setFeature("is_demo_user", true)
                .setFeature("has_custom_resolution", false);

        assertSame(options, chained);
        assertEquals("Steve", options.getVariables().get("auth_player_name"));
        assertEquals("token", options.getVariables().get("auth_access_token"));
        assertEquals(true, options.getFeatures().get("is_demo_user"));
        assertEquals(false, options.getFeatures().get("has_custom_resolution"));
    }

    @Test
    void laterAssignmentsOverridePreviousValues() {
        LaunchOptions options = LaunchOptions.create()
                .setVariable("version_name", "1.20.1")
                .setVariable("version_name", "1.21.11")
                .setFeature("is_demo_user", false)
                .setFeature("is_demo_user", true);

        assertEquals("1.21.11", options.getVariables().get("version_name"));
        assertEquals(true, options.getFeatures().get("is_demo_user"));
    }
}

