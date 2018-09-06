package edu.illinois.techservices.elmr;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CacheKey implementation using {@link SecureRandom} to generate keys.ß
 */
public class SecureRandomCacheKey implements CacheKey {

  private final SecureRandom secRandom = new SecureRandom();

  @Override
  public String generate() {
    return Long.toString(secRandom.nextLong());
  }

  @Override
  public byte[] encode(String key) {
    return Base64.getEncoder().encode(key.getBytes());
  }

  @Override
  public String decode(byte[] keybytes) {
    return new String(Base64.getDecoder().decode(keybytes));
  }
}
