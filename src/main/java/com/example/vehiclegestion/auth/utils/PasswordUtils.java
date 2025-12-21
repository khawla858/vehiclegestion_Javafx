package com.example.vehiclegestion.auth.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    // D'abord, découvrez quel hash est utilisé
    public static void debugHash(String password) {
        System.out.println("🔍 Debug hash pour: " + password);
        System.out.println("SHA-256: " + hashSHA256(password));
        System.out.println("SHA-1: " + hashSHA1(password));
        System.out.println("MD5: " + hashMD5(password));
    }

    public static String hashPassword(String password) {
        // Essayez d'abord SHA-256
        return hashSHA256(password);
    }

    private static String hashSHA256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String hashSHA1(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String hashMD5(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        // Essayez plusieurs algorithmes
        if (hashSHA256(password).equals(hashedPassword)) return true;
        if (hashSHA1(password).equals(hashedPassword)) return true;
        if (hashMD5(password).equals(hashedPassword)) return true;

        return false;
    }
}