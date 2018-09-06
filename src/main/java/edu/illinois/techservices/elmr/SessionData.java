package edu.illinois.techservices.elmr;

/**
 * Manages session data.
 * 
 * <p>
 * Session data is expected to be in the form of a String.
 */
public interface SessionData {

  /**
   * Property that can be used by an implementation to set a host name.
   */
  public static final String SESSION_DATA_HOSTNAME_SYSPROP =
      SessionData.class.getName() + ".hostname";

  /**
   * Property that can be used by an implementation to set a port.
   */
  public static final String SESSION_DATA_PORT_SYSPROP = SessionData.class.getName() + ".port";

  /**
   * Saves the given session data returning the key in the form of a byte array it was saved under.
   * 
   * @param sessionData data to save.
   * @return byte array of the key the data was saved under.
   */
  public byte[] save(String sessionData);

  /**
   * Returns the session data associated with the given key.
   * 
   * @param key byte array of the key the data was stored under.
   * @return the session data associated with the given key or {@code null} if not found.
   */
  public String get(byte[] key);

  /**
   * Deletes the session data associated with the given key.
   * 
   * <p>
   * Implementations can decide how to best handle keys that have no data associated with them.
   * 
   * @param key byte array of the key whose data is to be deleted.
   */
  public void destroy(byte[] key);

}
