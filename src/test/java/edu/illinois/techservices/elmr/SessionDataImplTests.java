package edu.illinois.techservices.elmr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Tests that session data can be saved, retrieved and destroyed.
 * 
 * <p>
 * This test should only be run if a redis store is running locally. Make sure that redis is running
 * and then run this with the system property
 * {@code edu.illinois.techservices.elmr.redis.CanConnect} set to {@code true}.
 */
@EnabledIfSystemProperty(named = "edu.illinois.techservices.elmr.redis.CanConnect",
    matches = "true")
class SessionDataImplTests {

  @Test
  void testSaveDataThenGetDataThenDestroyData() {
    try {
    var sd = new SessionDataImpl();
    var data = "some-test-data";
    var keybytes = sd.save(data);
    var retrieved = sd.get(keybytes);
    assertEquals(data, retrieved);
    sd.destroy(keybytes);
    var afterDestroy = sd.get(keybytes);
    assertNull(afterDestroy);
    } catch (JedisConnectionException e) {
      fail("Couldn't connect to redis to run tests. Check connection and try again.");
    }
  }
}
