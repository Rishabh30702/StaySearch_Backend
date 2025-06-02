package com.example.StaySearch.StaySearchBackend.PaymentGateway.HDFC;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HdfcEncryptionUtil {

    private static final String WORKING_KEY = "A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6"; // 32-character working key

    public static String encrypt(String plainText, String workingKey) throws Exception {
        SecretKeySpec key = new SecretKeySpec(workingKey.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }


    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec key = new SecretKeySpec(WORKING_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, "UTF-8");
    }
}
