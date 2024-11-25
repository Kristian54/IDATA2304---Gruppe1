package no.ntnu.cryptography;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * This class is responsible for handling cryptographic operations using symmetric encryption.
 *
 * <p>This class was made by following the tutorial on:
 * https://www.baeldung.com/java-aes-encryption-decryption</p>
 */
public class CryptoHandler {
  /**
   * Generates a secret key.
   * @param n The size of the key (128, 192 or 256 bits)
   * @return The generated key
   * @throws NoSuchAlgorithmException Throws en exception if the cryptographic algorithm isn't
   *                                  available.
   */
  public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(n);
    SecretKey key = keyGenerator.generateKey();
    return key;
  }

  /**
   * Generates an initialization vector.
   *
   * @return The generated initialization vector.
   */
  public static IvParameterSpec generateIv() {
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);
    return new IvParameterSpec(iv);
  }

  /**
   * Encrypts a string using the specified algorithm, key and initialization vector.
   *
   * @param algorithm The algorithm to use for encryption.
   * @param input The string to encrypt.
   * @param key The key to use for encryption.
   * @param iv The initialization vector to use for encryption.
   * @return The encrypted string.
   * @throws NoSuchPaddingException Thrown if the padding is not available.
   * @throws NoSuchAlgorithmException Thrown if the cryptographic algorithm is not available.
   * @throws InvalidAlgorithmParameterException Thrown if the algorithm parameters are invalid.
   * @throws InvalidKeyException Thrown if the key is invalid.
   * @throws BadPaddingException Thrown if the padding is bad.
   * @throws IllegalBlockSizeException Thrown if the block size is illegal.
   */
  public static String encrypt(String algorithm, String input, SecretKey key,
                               IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
      InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {

    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
    byte[] cipherText = cipher.doFinal(input.getBytes());
    return Base64.getEncoder()
        .encodeToString(cipherText);
  }

  /**
   * Decrypts a string using the specified algorithm, key and initialization vector.
   *
   * @param algorithm The algorithm to use for decryption.
   * @param cipherText The string to decrypt.
   * @param key The key to use for decryption.
   * @param iv The initialization vector to use for decryption.
   * @return The decrypted string.
   * @throws NoSuchPaddingException Thrown if the padding is not available.
   * @throws NoSuchAlgorithmException Thrown if the cryptographic algorithm is not available.
   * @throws InvalidAlgorithmParameterException Thrown if the algorithm parameters are invalid.
   * @throws InvalidKeyException Thrown if the key is invalid.
   * @throws BadPaddingException Thrown if the padding is bad.
   * @throws IllegalBlockSizeException Thrown if the block size is illegal.
   */
  public static String decrypt(String algorithm, String cipherText, SecretKey key,
                               IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
      InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {

    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.DECRYPT_MODE, key, iv);
    byte[] plainText = cipher.doFinal(Base64.getDecoder()
        .decode(cipherText));
    return new String(plainText);
  }
}
