package edu.illinois.techservices.elmr.servlets;

import java.net.ConnectException;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import edu.illinois.techservices.elmr.InMemorySessionData;
import edu.illinois.techservices.elmr.SessionData;
import edu.illinois.techservices.elmr.SessionDataImpl;

/**
 * Instantiates a {@link SessionData} object and caches it in a context parameter.
 * 
 * <p>
 * This listener assumes that the SessionData implementation has to connect to an external server.
 * Thus, a host name and port can be specified for this connection. Both the host name and the port
 * can be set 3 ways and are searched in this order:
 * 
 * <h3>Host name</h3>
 * <ol>
 * <li>The value of the system property {@code edu.illinois.techservices.elmr.SessionData.hostname}.
 * <li>The value of the context parameter
 * {@code edu.illinois.techservices.elmr.SessionData.hostname}.
 * <li>The default value {@code localhost} (if neither of the the above are set).
 * </ol>
 * 
 * <h3>Port</h3>
 * <ol>
 * <li>The value of the system property {@code edu.illinois.techservices.elmr.SessionData.port}.
 * <li>The value of the context parameter {@code edu.illinois.techservices.elmr.SessionData.port}.
 * <li>The default value that depends on the implementation (if neither of the the above are set).
 * </ol>
 * 
 * <p>
 * When set, a connection to the external datasource is established. If the connection fails, a
 * default in-memory cache is created and used instead.
 */
@WebListener
public class SessionDataContextListener implements ServletContextListener {

  private static final Logger LOGGER = Logger.getLogger(SessionDataContextListener.class.getName());

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    LOGGER.config("Initializing session data interface...");

    var hostname = System.getProperty(SessionData.SESSION_DATA_HOSTNAME_SYSPROP);
    if (hostname == null || hostname.isEmpty()) {
      hostname =
          sce.getServletContext().getInitParameter(SessionData.SESSION_DATA_HOSTNAME_SYSPROP);
      if (hostname == null || hostname.isEmpty()) {
        LOGGER.config("Setting host name to default value (port will be default value as well).");
      } else {
        LOGGER.config("Setting host name from context parameter "
            + SessionData.SESSION_DATA_HOSTNAME_SYSPROP);
      }
    } else {
      LOGGER.config(
          "Setting host name from system property " + SessionData.SESSION_DATA_HOSTNAME_SYSPROP);
    }

    Integer port = null;
    if (hostname != null && !hostname.isEmpty()) {
      port = Integer.getInteger(SessionData.SESSION_DATA_PORT_SYSPROP);
      if (port == null) {
        String p = sce.getServletContext().getInitParameter(SessionData.SESSION_DATA_PORT_SYSPROP);
        if (p != null && !p.isEmpty()) {
          port = Integer.parseInt(p);
        }
        if (port == null) {
          LOGGER.config("Setting port to default value.");
        } else {
          LOGGER.config(
              "Setting port from context parameter " + SessionData.SESSION_DATA_PORT_SYSPROP);
        }
      } else {
        LOGGER.config("Setting port from system property " + SessionData.SESSION_DATA_PORT_SYSPROP);
      }
    }

    SessionData sd = null;

    try {
      sd = (hostname == null || hostname.isEmpty()) ? new SessionDataImpl()
          : new SessionDataImpl(hostname, port);
    } catch (Exception e) {
      if (e.getCause() instanceof ConnectException) {
        LOGGER.warning(
            "Failed to connect to external datasource! Replacing SessionData with an in-memory implementation.");
        sd = new InMemorySessionData();
      }
    }
    sce.getServletContext()
        .setAttribute(SessionDataContextListener.class.getPackageName() + ".sessionData", sd);
    LOGGER.config("SessionData object configured; access with context property "
        + SessionDataContextListener.class.getPackageName() + ".sessionData");
  }
}
