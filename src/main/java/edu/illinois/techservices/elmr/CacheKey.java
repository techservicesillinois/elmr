package edu.illinois.techservices.elmr;

/**
 * Generates cache keys.
 * 
 * <p>
 * Implemenations need only return a String and should guarantee thread safety.
 */
public interface CacheKey {

  /**
   * Generates and returns a key.
   */
  String generate();

  /**
   * Encodes the given key to a byte array and returns it.
   * 
   * @param key the key to encode.
   */
  byte[] encode(String key);

  /**
   * Decodes the given byte array to a String and returns it.
   * 
   * @param keybytes byte array of the key to decode.
   */
  String decode(byte[] keybytes);
}
