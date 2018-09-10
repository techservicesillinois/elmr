package edu.illinois.techservices.elmr.servlets;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents data from an Apache configuration.
 */
public class ApacheConfig {

  private static int BUF_SZ = 0x1000;

  private static final Logger LOGGER = Logger.getLogger(ApacheConfig.class.getName());

  private static final Pattern JK_ENV_VAR_PATTERN = Pattern.compile("JkEnvVar\\s(\\w+)");

  private final byte[] apacheConfigBytes;

  /**
   * Construct an ApacheConfig object.
   * 
   * @param in InputStream representing an Apache configuration file.
   * @throws IOException if there's a problem reading the file.
   */
  public ApacheConfig(InputStream in) {
    var bos = new ByteArrayOutputStream();
    var buf = new byte[BUF_SZ];
    byte[] bytes = new byte[0];
    try {
      while (true) {
        var read = in.read(buf);
        if (read == -1) {
          break;
        }
        bos.write(buf, 0, read);
      }
      bytes = bos.toByteArray();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to cache input! Caching an empty byte array!", e);
      bytes = new byte[0];
    } finally {
      apacheConfigBytes = bytes;
    }
  }

  /**
   * Returns a List of the value of all lines defining {@code JkEnvVar}s.
   */
  public List<String> getJkEnvVars() {
    List<String> jkEnvVars = new ArrayList<>();
    String line = null;
    try (var reader =
        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(apacheConfigBytes)))) {
      Matcher m = null;
      while ((line = reader.readLine()) != null) {
        m = JK_ENV_VAR_PATTERN.matcher(line);
        while (m.find()) {
          String jkEnvVar = m.group(1);
          LOGGER.finer("Found JkEnvVar " + jkEnvVar);
          jkEnvVars.add(jkEnvVar);
        }
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to read configuration, returning empty list of env vars!",
          e);
    }
    return jkEnvVars;
  }

}
