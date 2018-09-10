package edu.illinois.techservices.elmr.servlets;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Loads an apache configuration file.
 * 
 * <p>
 * The name for the configuration file can be set 2 ways and are searched in this order:
 * <ol>
 * <li>The value of the system property
 * {@code edu.illinois.techservices.elmr.servlets.ApacheConfigFile}.
 * <li>The value of the context parameter
 * {@code edu.illinois.techservices.elmr.servlets.ApacheConfigFile}.
 * </ol>
 * 
 * <p>
 * If the file cannot be found, rather than fail the startup, this listener will load an empty
 * configuration and empty values will be returned from it. 
 */
@WebListener
public class ApacheConfigFileLoader implements ServletContextListener {

  private static final Logger LOGGER = Logger.getLogger(ApacheConfigFileLoader.class.getName());

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    LOGGER.config("Initializing data from Apache config file...");

    var apacheConfigFilename =
        System.getProperty(ConfigServlet.class.getPackageName() + ".ApacheConfigFile");
    if (apacheConfigFilename == null || apacheConfigFilename.isEmpty()) {
      apacheConfigFilename = sce.getServletContext()
          .getInitParameter(ConfigServlet.class.getPackageName() + ".ApacheConfigFile");
      if (apacheConfigFilename == null || apacheConfigFilename.isEmpty()) {
        LOGGER.warning(ConfigServlet.class.getPackageName()
            + ".ApacheConfigFile  was not set as a system property or context parameter. File will not be loaded.");

      } else {
        LOGGER.config("Reading file set by context parameter "
            + ConfigServlet.class.getPackageName() + ".ApacheConfigFile");
      }
    } else {
      LOGGER.config("Reading file set by system property " + ConfigServlet.class.getPackageName()
          + ".ApacheConfigFile");
    }

    ApacheConfig acf = null;
    try {
      acf = (apacheConfigFilename == null || apacheConfigFilename.isEmpty())
          ? new ApacheConfig(new ByteArrayInputStream(new byte[0]))
          : new ApacheConfig(new FileInputStream(apacheConfigFilename));
    } catch (NullPointerException | FileNotFoundException e) {
      LOGGER.warning("Apache config file not found. Creating an empty Apache config.");
      acf = new ApacheConfig(new ByteArrayInputStream(new byte[0]));
    }

    sce.getServletContext()
        .setAttribute(ApacheConfigFileLoader.class.getPackageName() + ".ApacheConfig", acf);
    LOGGER.config("Apache configuration cached; access with context property "
        + ApacheConfigFileLoader.class.getPackageName() + ".ApacheConfig");
  }
}
