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

  private final JedisPool jp;

  private final CacheKey cacheKey = new SecureRandomCacheKey();

  private final SessionData fallbackImpl;

  /**
   * Connects to a Redis store at {@value #DEFAULT_HOSTNAME} on port {@value #DEFAULT_PORT}.
   */
  public SessionDataImpl() {
    this(DEFAULT_HOSTNAME, DEFAULT_PORT, new InMemorySessionData());
  }

  /**
   * Connects to a Redis store at the given host name on the given port.
   * 
   * @param hostname host of the Redis store.
   * @param port     Redis port.
   */
  public SessionDataImpl(String hostname, int port, SessionData fallbackImpl) {
    this.fallbackImpl = fallbackImpl;
    jp = new JedisPool(new JedisPoolConfig(), hostname, port);
    LOGGER.config("Constructed " + SessionDataImpl.class.getName() + " with hostname = " + hostname
        + ", port = " + port);
  }

  @Override
  public byte[] save(String sessionData) {
    return doSaveReturningKey(sessionData);
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
  private byte[] doSaveReturningKey(String sessionData) {
    String key = cacheKey.generate();
    try (Jedis j = jp.getResource()) {
      // Key generation is random but not perfect so make sure a key that doesn't already exist is
      // generated.
      while (j.exists(key)) {
        key = cacheKey.generate();
      }
      j.set(key, sessionData);
    } catch (JedisConnectionException e) {
      LOGGER.warning("Failed to connect to redis! Using fallback implementation!");
      return fallbackImpl.save(sessionData);
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
    } catch (JedisConnectionException e) {
      LOGGER.warning("Failed to connect to redis! Using fallback implementation!");
      return fallbackImpl.get(key);
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
    } catch (JedisConnectionException e) {
      LOGGER.warning("Failed to connect to redis! Using fallback implmentation!");
      fallbackImpl.destroy(key);
    }
  }
}
