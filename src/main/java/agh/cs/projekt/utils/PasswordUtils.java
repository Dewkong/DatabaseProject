package agh.cs.projekt.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;

public class PasswordUtils {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    public static String getSalt(int length) {
        Random random = new SecureRandom();

        StringBuilder salt = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            salt.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return new String(salt);
    }

    public static String encryptPassword(String password, String salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] encryptedPassword = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(encryptedPassword);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        }
    }

    public static boolean verifyPassword(String providedPassword, String encryptedPassword, String salt) {
        String newlyGeneratedPassword = encryptPassword(providedPassword, salt);
        return newlyGeneratedPassword.equalsIgnoreCase(encryptedPassword);
    }

}
