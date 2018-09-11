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
        System.getProperty(PackageConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME);
    if (apacheConfigFilename == null || apacheConfigFilename.isEmpty()) {
      apacheConfigFilename = sce.getServletContext()
          .getInitParameter(PackageConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME);
      if (apacheConfigFilename == null || apacheConfigFilename.isEmpty()) {
        LOGGER.warning(PackageConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME
            + " was not set as a system property or context parameter. File will not be loaded.");

      } else {
        LOGGER.config("Reading file set by context parameter "
            + PackageConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME);
      }
    } else {
      LOGGER.config("Reading file set by system property "
          + PackageConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME);
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

    sce.getServletContext().setAttribute(PackageConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME,
        acf);
    LOGGER.config("Apache configuration cached; access with context property "
        + PackageConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME);
  }
}
