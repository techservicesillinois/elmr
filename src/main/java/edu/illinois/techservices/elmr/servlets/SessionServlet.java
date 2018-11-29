package edu.illinois.techservices.elmr.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.illinois.techservices.elmr.Json;
import edu.illinois.techservices.elmr.SessionData;

/**
 * Servlet that captures attribute data from Shibboleth to store it.
 */
@WebServlet(urlPatterns = {"/session"}, name = "SessionServlet")
public class SessionServlet extends HttpServlet {

  private static final long serialVersionUID = 1755921268489294474L;

  private static final Logger LOGGER = Logger.getLogger(SessionServlet.class.getName());

  private String uniqueUserIdentifier;

  @Override
  public void init() {
    uniqueUserIdentifier = System.getProperty(ServletConstants.UNIQUE_USER_ID_PARAM_NAME);

    if (uniqueUserIdentifier == null || uniqueUserIdentifier.isEmpty()) {
      uniqueUserIdentifier =
          getServletContext().getInitParameter(ServletConstants.UNIQUE_USER_ID_PARAM_NAME);
    }

    if (uniqueUserIdentifier == null || uniqueUserIdentifier.isEmpty()) {
      uniqueUserIdentifier = ServletConstants.DEFAULT_UNIQUE_USER_ID;
    }

    LOGGER.config("Unique user identifier attribute name: " + uniqueUserIdentifier);
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    var mode =
        (request.getParameter("mode") == null || request.getParameter("mode").isEmpty()) ? "create"
            : request.getParameter("mode");

    if (mode.equals("logout")) {
      if (sessionDestroyed(request, response)) {
        redirectToLogout(response);
      }
    } else {
      var serviceUrl = request.getCookies() != null ? getServiceUrl(request.getCookies()) : "";


      try {
        if (serviceUrl == null || serviceUrl.isEmpty()) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST,
              "Redirect back to service was not set. Set a cookie with the name '"
                  + ServletConstants.SERVICE_URL_COOKIE_NAME + "'.");
        } else if (request.getAttribute(uniqueUserIdentifier) == null) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST,
              "Failed to find a request attribute named " + uniqueUserIdentifier
                  + ". Please check your web server configuration.");
        } else if (sessionCreated(request, response)) {
          response.sendRedirect(serviceUrl);
        }
      } catch (RuntimeException e) {
        // Assume the worst has happened and there is no connection to the session data store.
        // Set a 503 status, log the exception and return.
        LOGGER.log(Level.WARNING, "A problem occurred establishing the session.", e);
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
            "Internal failure: could not connect to session data store!");
      }
    }
    return;
  }

  private boolean sessionCreated(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    var sd = (SessionData) getServletContext()
        .getAttribute(ServletConstants.SESSION_DATA_CONTEXT_PARAM_NAME);
    if (sd == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Session data store not configured!");
      return false;
    }

    @SuppressWarnings("unchecked")
    List<String> userAttributes = (List<String>) getServletContext()
        .getAttribute(ServletConstants.ATTRIBUTES_CONTEXT_PARAM_NAME);

    Map<String, Object> output = Map.of();
    if (userAttributes != null && !userAttributes.isEmpty()) {
      // Generate a Map with attribute names as keys and Shibboleth attribute values as values.
      output = userAttributes.stream()
          .filter(userAttr -> Objects.nonNull(request.getAttribute(userAttr)))
          .collect(Collectors.toMap(Function.identity(), userAttr -> {
            String attr = (String) request.getAttribute(userAttr);
            if (attr.indexOf(';') > 0) {
              return Arrays.asList(attr.split(";"));
            }
            return attr;
          }));
    }
    if (output.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      return false;
    } else {
      var json = Json.renderObject(output);
      var preComputedKey = request.getAttribute(uniqueUserIdentifier).toString().getBytes();
      var key = sd.save(preComputedKey, json);
      var cookie = new Cookie(ServletConstants.SESSION_KEY_COOKIE_NAME, new String(key));
      if (!isSecureCookiesDisabled()) {
        cookie.setSecure(true);
        if (!request.isSecure()) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST,
              "Can't set secure cookies for non-HTTPS requests! Set "
                  + ServletConstants.SESSION_KEY_DISABLE_SECURE
                  + " as a context parameter or system property and restart the server or use HTTPS.");
          return false;
        }
      }
      cookie.setPath("/");
      response.addCookie(cookie);
      LOGGER.info(
          "Session " + request.getAttribute(uniqueUserIdentifier).toString() + " established.");
      return true;
    }
  }

  private String getServiceUrl(Cookie[] cookies) {
    Optional<Cookie> maybeHaveServiceUrlCookie = Arrays.stream(cookies)
        .filter(c -> c.getName().equals(ServletConstants.SERVICE_URL_COOKIE_NAME)).findAny();
    return maybeHaveServiceUrlCookie.isPresent() ? maybeHaveServiceUrlCookie.get().getValue() : "";
  }

  private boolean sessionDestroyed(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    var sd = (SessionData) getServletContext()
        .getAttribute(ServletConstants.SESSION_DATA_CONTEXT_PARAM_NAME);
    if (sd == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Session data store not configured!");
      return false;
    }

    var cookies = request.getCookies();
    var sessionKeyCookieDestroyed = false;
    var serviceUrlCookieDestroyed = false;

    // These cookies may or may not be set at this point. This loop makes sure that if the Cookies
    // are still there that they are destroyed.
    if (cookies != null) {
      for (Cookie c : cookies) {
        if (c.getName().equals(ServletConstants.SESSION_KEY_COOKIE_NAME)) {
          byte[] key = c.getValue().getBytes();
          sd.destroy(key);
          response.addCookie(createCookieToUnset(ServletConstants.SESSION_KEY_COOKIE_NAME));
          sessionKeyCookieDestroyed = true;
        } else if (c.getName().equals(ServletConstants.SERVICE_URL_COOKIE_NAME)) {
          response.addCookie(createCookieToUnset(ServletConstants.SERVICE_URL_COOKIE_NAME));
          serviceUrlCookieDestroyed = true;
        }
        if (sessionKeyCookieDestroyed && serviceUrlCookieDestroyed) {
          break;
        }
      }
    }
    // If we get this far, logout has happened.
    LOGGER.info("Session " + request.getAttribute(uniqueUserIdentifier).toString() + " destroyed.");
    return true;
  }

  private Cookie createCookieToUnset(String name) {
    Cookie toUnset = new Cookie(name, null);
    toUnset.setMaxAge(0);
    toUnset.setPath("/");
    return toUnset;
  }

  private void redirectToLogout(HttpServletResponse response) throws IOException, ServletException {
    var logoutUrl = System.getProperty(ServletConstants.LOGOUT_URL,
        getServletContext().getInitParameter(ServletConstants.LOGOUT_URL));
    if (logoutUrl == null || logoutUrl.isEmpty()) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Redirect URL for logout not set!");
    } else {
      response.sendRedirect(logoutUrl);
    }
  }

  private boolean isSecureCookiesDisabled() {
    var disableSecureCookies = Boolean.getBoolean(ServletConstants.SESSION_KEY_DISABLE_SECURE);
    if (!disableSecureCookies) {
      var disableSecureCookiesValue =
          getServletContext().getInitParameter(ServletConstants.SESSION_KEY_DISABLE_SECURE);
      if (disableSecureCookiesValue != null && !disableSecureCookiesValue.isEmpty()) {
        disableSecureCookies = Boolean.valueOf(disableSecureCookiesValue);
      }
    }
    return disableSecureCookies;
  }
}
