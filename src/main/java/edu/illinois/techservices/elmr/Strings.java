package edu.illinois.techservices.elmr;

/**
 * Utilities for String objects.
 */
public class Strings {

  private Strings() {
    // Empty constructor prevents instantiation.
  }

  /**
   * Returns {@code true} if the given parameter is not null and not empty.
   * 
   * @param s a String.
   */
  public static boolean notNullAndNotEmpty(String s) {
    return s != null && !s.isEmpty();
  }

  /**
   * Returns a String where the first character is capitalized and the rest are lower case or
   * {@code null} if the given String is {@code null}.
   * 
   * @param key the String to normalize.
   */
  public static String normalize(String key) {
    if (key == null) {
      return null;
    }
    int len = key.length();
    if (len == 0) {
      return key;
    }
    char[] b = key.toCharArray();
    if (b[0] >= 'a' && b[0] <= 'z') {
      b[0] = (char) (b[0] - ('a' - 'A'));
    }
    for (int i = 1; i < len; i++) {
      if (b[i] >= 'A' && b[i] <= 'Z') {
        b[i] = (char) (b[i] + ('a' - 'A'));
      }
    }
    return new String(b);
  }
}
