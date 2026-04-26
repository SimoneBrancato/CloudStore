package com.cloudstore.server.service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordHasher {

    // Default constructor
    private PasswordHasher() {
    }

    /**
        * Hashes a raw password using SHA-256.
        * @param rawPassword The raw password to hash.
        * @return The hashed password as a hexadecimal string.
        * @throws IllegalArgumentException If the raw password is null.
        * @throws IllegalStateException If the SHA-256 algorithm is not available.
    **/
    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return toHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
        * Compares a raw password with a stored hashed password.
        * @param rawPassword The raw password to compare.
        * @param storedPassword The stored hashed password to compare against.
        * @return True if the passwords match, false otherwise.
    **/
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        String hashedInput = hash(rawPassword);
        return constantTimeEquals(hashedInput, storedPassword);
    }   

    /**
        * Converts a byte array to a hexadecimal string.
        * @param bytes The byte array to convert.
        * @return The hexadecimal string.
    **/
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
        * Compares two strings in constant time to prevent timing attacks.
        * @param a The first string to compare.
        * @param b The second string to compare.
        * @return True if the strings are equal, false otherwise.
    **/
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
