package no.ntnu.cryptography;

import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * This class holds the cryptographic details used for encryption and decryption.
 */
public class CryptoDetails{
  protected static final SecretKey KEY;
  protected static final IvParameterSpec IV;

  static {
    try {
      KEY = CryptoHandler.generateKey(128);
      IV = CryptoHandler.generateIv();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Prevent instantiation of this class.
   */
  protected CryptoDetails() {}
}