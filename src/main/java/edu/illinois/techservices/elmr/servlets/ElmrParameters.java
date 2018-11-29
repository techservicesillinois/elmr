package edu.illinois.techservices.elmr.servlets;

import java.util.logging.Logger;
import javax.servlet.ServletContext;

final class ElmrParameters {

  private static final Logger LOGGER = Logger.getLogger(ElmrParameters.class.getName());

  private ElmrParameters() {
    // empty constructor to prevent instantiation and extension
  }

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

  static Boolean getBoolean(ServletContext sc, String name, Boolean defaultValue) {
    Boolean paramValue = Boolean.getBoolean(name);
    if (paramValue.equals(defaultValue)) {
      LOGGER.config("System property " + name
          + " has the same as the default value, checking servlet context parameters.");
      paramValue = Boolean.valueOf(sc.getInitParameter(name));
      if (paramValue.equals(defaultValue)) {
        LOGGER.config("Using default value " + defaultValue + " for " + name + ".");
        paramValue = defaultValue;
      } else {
        LOGGER.config(
            "Using value from servlet context parameter " + name + " of " + paramValue + ".");
      }
    }
    return paramValue;
  }
}
