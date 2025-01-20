package com.pcistudio.task.procesor.util;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class SymmetricEncryption {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SymmetricEncryption() {
    }

    private static final int TAG_BIT_LENGTH = 128; // 16 bytes tag for integrity

    private static SecretKey generateSecretKey(int keySize) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(keySize, SECURE_RANDOM);
        return keyGen.generateKey();
    }

    private static byte[] generateIV(int ivSize) {
        byte[] iv = new byte[ivSize];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }

    public static byte[] encrypt(String plaintext, SecretKey key, byte[] iv, byte[] aad) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        cipher.updateAAD(aad);
        return cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    private static String decrypt(byte[] cipherText, SecretKey key, byte[] iv, byte[] aad) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        cipher.updateAAD(aad);
        byte[] decryptedBytes = cipher.doFinal(cipherText);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {

        String plaintext = "Hello, World!";
        SecretKey key = generateSecretKey(128); // Generate a 128-bit key
        String aad = "user:1";
        // Encrypt the plaintext
        byte[] iv = generateIV(12); // Random IV
        byte[] encryptedBytes = encrypt(plaintext, key, iv, aad.getBytes(StandardCharsets.UTF_8));
        System.out.println("Encrypted Text: " + Base64.getEncoder().encodeToString(encryptedBytes));
//            aad = "user:2"; fail to decrypt
        // Decrypt the encrypted text
        String decryptedText = decrypt(encryptedBytes, key, iv, aad.getBytes(StandardCharsets.UTF_8));
        System.out.println("Decrypted Text: " + decryptedText);

    }
}