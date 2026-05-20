package com.dervarex.minified.auth;

public final class AuthUtils {
    private AuthUtils() {
    }

    public static String normalizeToken(String token) {
        if (token == null) {
            return "";
        }
        return token.trim();
    }

    public static boolean isBearerToken(String token) {
        if (token == null) {
            return false;
        }
        String trimmed = token.trim();
        return trimmed.regionMatches(true, 0, "Bearer ", 0, 7) && trimmed.length() > 7;
    }
}

