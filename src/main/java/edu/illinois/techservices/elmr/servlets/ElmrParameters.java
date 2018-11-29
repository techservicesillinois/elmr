package edu.illinois.techservices.elmr.servlets;

import java.util.logging.Logger;
import javax.servlet.ServletContext;

/**
 * Utilities for retrieving parameter values for the application.
 * 
 * <p>
 * The application will retrieve parameters in the order:
 * <ol>
 * <li>System Property value
 * <li>Context parameter value
 * <li>Default value
 * </ol>
 * 
 * The utility methods here are used to encourage consistency across the entire application.
 */
final class ElmrParameters {

  private static final Logger LOGGER = Logger.getLogger(ElmrParameters.class.getName());

  private ElmrParameters() {
    // empty constructor to prevent instantiation and extension
  }

  /**
   * Return a String-valued parameter.
   * 
   * @param sc           the ServletContext
   * @param name         the name of the parameter
   * @param defaultValue a default value if neither a system property nor servlet context parameter
   *                     with the given name are set.
   * 
   * @return the value as a String.
   */
  static String getString(ServletContext sc, String name, String defaultValue) {
    String paramValue = System.getProperty(name, defaultValue);
    if (paramValue.equals(defaultValue)) {
      LOGGER.config("System property " + name + " not set, checking servlet context parameters.");
      paramValue = sc.getInitParameter(name);
      if (paramValue == null || paramValue.isEmpty()) {
        LOGGER.config("Using default value " + defaultValue + " for " + name + ".");
        paramValue = defaultValue;
      } else {
        LOGGER.config(
            "Using value from servlet context parameter " + name + " of " + paramValue + ".");
      }
    }
    return paramValue;
  }

  /**
   * Return an Integer-valued parameter.
   * 
   * @param sc           the ServletContext
   * @param name         the name of the parameter
   * @param defaultValue a default value if neither a system property nor servlet context parameter
   *                     with the given name are set.
   * 
   * @return the value as an Integer.
   */
  static Integer getInteger(ServletContext sc, String name, Integer defaultValue) {
    var paramValue = Integer.getInteger(name, defaultValue);
    if (paramValue.equals(defaultValue)) {
      LOGGER.config("System property " + name + " not set, checking servlet context parameters.");
      try {
        if (sc.getInitParameter(name) != null && !sc.getInitParameter(name).isEmpty()) {
          paramValue = Integer.parseInt(sc.getInitParameter(name));
        } else {
          paramValue = null;
        }
      } catch (NumberFormatException e) {
        LOGGER.config(() -> "Error parsing integer " + sc.getInitParameter(name)
            + ", setting default value.");
        paramValue = defaultValue;
      }
      if (paramValue == null) {
        LOGGER.config("Using default value " + defaultValue + " for " + name + ".");
        paramValue = defaultValue;
      } else {
        LOGGER.config(
            "Using value from servlet context parameter " + name + " of " + paramValue + ".");
      }
    }
    return paramValue;
  }

  /**
   * Return a Boolean-valued parameter.
   * 
   * @param sc           the ServletContext
   * @param name         the name of the parameter
   * @param defaultValue a default value if neither a system property nor servlet context parameter
   *                     with the given name are set.
   * 
   * @return the value as a Boolean.
   */
  static Boolean getBoolean(ServletContext sc, String name, Boolean defaultValue) {
    Boolean paramValue = Boolean.getBoolean(name);
    if (paramValue.equals(defaultValue)) {
      LOGGER.config("System property " + name
          + " has the same as the default value, checking servlet context parameters.");
      paramValue = 
      if (sc.getInitParameter(name) == null) {
        paramValue = defaultValue;
      } else {
        paramValue = Boolean.valueOf(sc.getInitParameter(name));
      }
      if (paramValue.equals(defaultValue)) {
        LOGGER.config("Using default value " + defaultValue + " for " + name + ".");
      } else {
        LOGGER.config(
            "Using value from servlet context parameter " + name + " of " + paramValue + ".");
      }
    }
    return paramValue;
  }
}
