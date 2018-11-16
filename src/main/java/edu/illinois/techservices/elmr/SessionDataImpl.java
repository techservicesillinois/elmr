package edu.illinois.techservices.elmr;

import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * SessionData implementation connecting to a Redis store.
 */
public class SessionDataImpl implements SessionData {

  private static final Logger LOGGER = Logger.getLogger(SessionDataImpl.class.getName());

  public static final String DEFAULT_HOSTNAME = "localhost";

  public static final int DEFAULT_PORT = 6379;

  public static final int DEFAULT_MIN_CONNECTIONS = JedisPoolConfig.DEFAULT_MIN_IDLE;

  public static final int DEFAULT_MAX_CONNECTIONS = JedisPoolConfig.DEFAULT_MAX_IDLE;

  public static final String MIN_CONNECTIONS_SYSPROP =
      SessionDataImpl.class.getName() + ".minConnections";

  public static final String MAX_CONNECTIONS_SYSPROP =
      SessionDataImpl.class.getName() + ".maxConnections";

  private static final byte[] EMPTY = new byte[0];

  private final JedisPool jp;

  private final CacheKey cacheKey = new SecureRandomCacheKey();

  /**
   * Connects to a Redis store at {@value #DEFAULT_HOSTNAME} on port {@value #DEFAULT_PORT}.
   */
  public SessionDataImpl() {
    this(System.getProperty(SessionData.SESSION_DATA_HOSTNAME_SYSPROP, DEFAULT_HOSTNAME),
        Integer.getInteger(SessionData.SESSION_DATA_PORT_SYSPROP, DEFAULT_PORT));
  }

  /**
   * Connects to a Redis store at the given host name on the given port.
   * 
   * @param hostname host of the Redis store.
   * @param port     Redis port.
   */
  public SessionDataImpl(String hostname, int port) {
    this(hostname, port, Integer.getInteger(MIN_CONNECTIONS_SYSPROP, DEFAULT_MIN_CONNECTIONS),
        Integer.getInteger(MAX_CONNECTIONS_SYSPROP, DEFAULT_MAX_CONNECTIONS));
  }

  public SessionDataImpl(String hostname, int port, int minConnections, int maxConnections) {
    JedisPoolConfig jpConfig = new JedisPoolConfig();
    if (minConnections != DEFAULT_MIN_CONNECTIONS) {
      jpConfig.setMinIdle(minConnections);
    }
    if (maxConnections != DEFAULT_MAX_CONNECTIONS) {
      jpConfig.setMaxTotal(maxConnections);
    }
    jp = new JedisPool(jpConfig, hostname, port);
    LOGGER.config("Constructed " + SessionDataImpl.class.getName() + " with hostname = " + hostname
        + ", port = " + port);
  }

  public byte[] save(byte[] key, String sessionData) {
    return key;
  }

  @Override
  public byte[] save(String sessionData) {
    return doSaveReturningKey(EMPTY, sessionData);
  }

  @Override
  public String get(byte[] key) {
    return doGetReturningData(key);
  }

  @Override
  public void destroy(byte[] key) {
    doDestroy(key);
  }

  /*
   * Indirect method to avoid public methods overridden in a subclass.
   */
  private byte[] doSaveReturningKey(byte[] preComputedKey, String sessionData) {
    String key = "";
    try (Jedis j = jp.getResource()) {
      if (preComputedKey == null || preComputedKey.length == 0) {
        key = cacheKey.generate();
        // Key generation is random but not perfect so make sure a key that doesn't already exist is
        // generated.
        while (j.exists(key)) {
          key = cacheKey.generate();
        }
      } else {
        key = new String(preComputedKey);
      }
      j.set(key, sessionData);
    }
    return cacheKey.encode(key);
  }

  /*
   * Indirect method to avoid public methods overridden in a subclass.
   */
  private String doGetReturningData(byte[] key) {
    String decodedKey = cacheKey.decode(key);
    String sessionData = null;
    try (Jedis j = jp.getResource()) {
      sessionData = j.get(decodedKey);
    }
    return sessionData;
  }

  /*
   * Indirect method to avoid public methods overridden in a subclass.
   */
  private void doDestroy(byte[] key) {
    String decodedKey = cacheKey.decode(key);
    try (Jedis j = jp.getResource()) {
      j.del(decodedKey);
    }
  }

  @Override
  public boolean isConnected() {
    try (Jedis j = jp.getResource()) {
      var response = j.ping();
      return response.equalsIgnoreCase("PONG");
    } catch (JedisConnectionException e) {
      return false;
    }
  }
}
