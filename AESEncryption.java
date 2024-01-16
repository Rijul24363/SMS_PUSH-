package sendSms;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESEncryption {

	 public static String encryptAES256ECB(String message, String key) throws Exception {
	        // Ensure the key is 32 bytes (256 bits) for AES-256
	        if (key.length() != 32) {
	            throw new IllegalArgumentException("Key must be 32 bytes long for AES-256.");
	        }

	        // Create a SecretKey object from the provided key
	        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");

	        // Create and initialize the Cipher object for AES-256 in ECB mode
	        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

	        // Encrypt the message
	        byte[] encryptedBytes = cipher.doFinal(message.getBytes());

	        // Base64 encode the result for easy representation
	        return Base64.getEncoder().encodeToString(encryptedBytes);
	    }
	 
	 
	 public static String decryptAES256ECB(String encryptedMessage, String key) throws Exception {
	        // Ensure the key is 32 bytes (256 bits) for AES-256
	        if (key.length() != 32) {
	            throw new IllegalArgumentException("Key must be 32 bytes long for AES-256.");
	        }

	        // Decode the encrypted message from Base64
	        byte[] decodedMessage = Base64.getDecoder().decode(encryptedMessage);

	        // Create a SecretKey object from the provided key
	        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");

	        // Create and initialize the Cipher object for AES-256 in ECB mode
	        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	        cipher.init(Cipher.DECRYPT_MODE, secretKey);

	        // Decrypt the message
	        byte[] decryptedBytes = cipher.doFinal(decodedMessage);

	        // Convert decrypted bytes to string
	        return new String(decryptedBytes);
	    }

    public static void main(String[] args) {
        try {             
            String key = "gVkYp3s5v8y/B?E(H+MbQeThWmZq4t7w"; // 32-byte key in hexadecimal format
            String message = "Testing Message";
            
            String encryptedMessage = encryptAES256ECB(message, key);
            String decryptMessage = decryptAES256ECB(encryptedMessage, key);
            System.out.println("Encrypted Message: " + encryptedMessage);
            System.out.println("Encrypted Message: " + decryptMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
