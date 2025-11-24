package com.cvetti.server.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    Base64.getDecoder().decode(salt),
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao hashear a senha!", e);
        }
    }

    public static boolean checkPassword(String rawPassword, String storedHash, String salt) {
        String newHash = hashPassword(rawPassword, salt);
        return newHash.equals(storedHash);
    }
}
