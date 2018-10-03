package edu.illinois.techservices.elmr.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import edu.illinois.techservices.elmr.InMemorySessionData;

class SessionServletTest {

  private static final String CONTEXT_PATH = "/elmr";

  private static final String LOGOUT_URL = "/Shibboleth.sso/Logout";

  private static final String REQUEST_URI = CONTEXT_PATH + "/session";

  private static final String SERVICE_URL = "/elmr/attributes";

  private static final String CANNED_SESSION_DATA = "canned_session_data";

  private static final String SESSION_SERVLET_NAME = "SessionServlet";

  private static final InMemorySessionData sessionData = new InMemorySessionData();

  private static final Map<String, List<String>> CREATE_REQUEST_PARAMETERS =
      Map.of("mode", List.of("create"));

  private static final Map<String, List<String>> LOGOUT_REQUEST_PARAMETERS =
      Map.of("mode", List.of("logout"));

  private static final List<String> SHIBBOLETH_ATTRIBUTE_NAMES_IN_MAP = List.of("displayName",
      "uid", "eduPersonPrincipalName", "eduPersonTargetedID", "eduPersonAffiliation");

  private static final Map<String, Object> SERVLET_CONTEXT_ATTRIBUTES =
      Map.of(ServletConstants.SESSION_DATA_CONTEXT_PARAM_NAME, sessionData,
          ServletConstants.ATTRIBUTES_CONTEXT_PARAM_NAME, SHIBBOLETH_ATTRIBUTE_NAMES_IN_MAP);

  private static final Map<String, String> SERVLET_CONTEXT_INIT_PARAMETERS =
      Map.of(ServletConstants.LOGOUT_URL, LOGOUT_URL);

  private static final Map<String, Object> SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE =
      Map.of("displayName", "Test User", "uid", "testuser1", "eduPersonPrincipalName",
          "testuser1@example.com", "eduPersonTargetedID", "testuser1@example.com-1234",
          "eduPersonAffiliation", "person;test;staff");

  @AfterEach
  void tearDown() {
    sessionData.clear();
  }

  @Test
  void testCreateSessionNoRequestParameter() {
    var servletContextInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(SERVLET_CONTEXT_ATTRIBUTES).contextPath(CONTEXT_PATH).build();
    var servletContext = ProxyFactories.createServletContextProxy(servletContextInvocationHandler);

    var servletConfigInvocationHandler = new ServletApiInvocationHandler.Builder()
        .servletName(SESSION_SERVLET_NAME).servletContext(servletContext).build();
    var servletConfig = ProxyFactories.createServletConfigProxy(servletConfigInvocationHandler);

    var requestInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE).requestUri(REQUEST_URI)
        .cookies(List.of(new Cookie(ServletConstants.SERVICE_URL_COOKIE_NAME, SERVICE_URL)))
        .servletContext(servletContext).build();
    var httpServletRequest = ProxyFactories.createHttpServletRequestProxy(requestInvocationHandler);

    var responseInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var httpServletResponse =
        ProxyFactories.createHttpServletResponseProxy(responseInvocationHandler);

    // Set up Servlet for run.
    var sessionServlet = new SessionServlet();

    try {

      // This is a call Tomcat has to do; we do it here to simulate that.
      sessionServlet.init(servletConfig);

      // Execute the request.
      sessionServlet.service(httpServletRequest, httpServletResponse);

    } catch (Exception e) {
      fail("Unexpected error!", e);
      if (e.getCause() != null) {
        e.getCause().printStackTrace();
      }
    }

    // Asserts
    assertEquals(HttpServletResponse.SC_FOUND, responseInvocationHandler.getStatusCode());

    // assert session key cookie is set
    Optional<Cookie> maybeHaveSessionKeyCookie =
        responseInvocationHandler.getResponseCookiesView().stream()
            .filter(c -> c.getName().equals(ServletConstants.SESSION_KEY_COOKIE_NAME)).findAny();
    assertTrue(maybeHaveSessionKeyCookie.isPresent(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " cookie not set!");
    assertNotNull(maybeHaveSessionKeyCookie.get().getValue(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " cookie value not set!");

    String json = sessionData.get(maybeHaveSessionKeyCookie.get().getValue().getBytes());
    for (String key : SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE.keySet()) {
      assertTrue(json.contains(key), key + " expected but not found in " + json + "!");
    }
    for (Object value : SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE.values()) {
      String v = value.toString();
      if (v.indexOf(';') > 0) {
        for (String part : v.split(";")) {
          assertTrue(json.contains(part), part + " expected but not found in " + json + "!");
        }
      } else {
        assertTrue(json.contains(value.toString()),
            value + " expected but not found in " + json + "!");
      }
    }
    assertTrue(responseInvocationHandler.sendRedirectWasCalled(),
        "HttpServletResponse.sendRedirect(String) was not called!");
  }

  @Test
  void testCreateSessionWithRequestParameter() {
    var servletContextInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(SERVLET_CONTEXT_ATTRIBUTES).contextPath(CONTEXT_PATH).build();
    var servletContext = ProxyFactories.createServletContextProxy(servletContextInvocationHandler);

    var servletConfigInvocationHandler = new ServletApiInvocationHandler.Builder()
        .servletName(SESSION_SERVLET_NAME).servletContext(servletContext).build();
    var servletConfig = ProxyFactories.createServletConfigProxy(servletConfigInvocationHandler);

    var requestInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE)
        .addRequestParameters(CREATE_REQUEST_PARAMETERS).requestUri(REQUEST_URI)
        .cookies(List.of(new Cookie(ServletConstants.SERVICE_URL_COOKIE_NAME, SERVICE_URL)))
        .servletContext(servletContext).build();
    var httpServletRequest = ProxyFactories.createHttpServletRequestProxy(requestInvocationHandler);

    var responseInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var httpServletResponse =
        ProxyFactories.createHttpServletResponseProxy(responseInvocationHandler);

    // Set up Servlet for run.
    var sessionServlet = new SessionServlet();

    try {

      // This is a call Tomcat has to do; we do it here to simulate that.
      sessionServlet.init(servletConfig);

      // Execute the request.
      sessionServlet.service(httpServletRequest, httpServletResponse);

    } catch (Exception e) {
      fail("Unexpected error!", e);
      if (e.getCause() != null) {
        e.getCause().printStackTrace();
      }
    }

    // Asserts
    assertEquals(HttpServletResponse.SC_FOUND, responseInvocationHandler.getStatusCode());

    // assert session key cookie is set
    Optional<Cookie> maybeHaveSessionKeyCookie =
        responseInvocationHandler.getResponseCookiesView().stream()
            .filter(c -> c.getName().equals(ServletConstants.SESSION_KEY_COOKIE_NAME)).findAny();
    assertTrue(maybeHaveSessionKeyCookie.isPresent(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " cookie not set!");
    assertNotNull(maybeHaveSessionKeyCookie.get().getValue(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " cookie value not set!");

    String json = sessionData.get(maybeHaveSessionKeyCookie.get().getValue().getBytes());
    for (String key : SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE.keySet()) {
      assertTrue(json.contains(key), key + " expected but not found in " + json + "!");
    }
    for (Object value : SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE.values()) {
      String v = value.toString();
      if (v.indexOf(';') > 0) {
        for (String part : v.split(";")) {
          assertTrue(json.contains(part), part + " expected but not found in " + json + "!");
        }
      } else {
        assertTrue(json.contains(value.toString()),
            value + " expected but not found in " + json + "!");
      }
    }
    assertTrue(responseInvocationHandler.sendRedirectWasCalled(),
        "HttpServletResponse.sendRedirect(String) was not called!");
  }

  @Test
  void testServiceUrlCookieNotSetForRedirectOnCreate() {
    var servletContextInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(SERVLET_CONTEXT_ATTRIBUTES).contextPath(CONTEXT_PATH).build();
    var servletContext = ProxyFactories.createServletContextProxy(servletContextInvocationHandler);

    var servletConfigInvocationHandler = new ServletApiInvocationHandler.Builder()
        .servletName(SESSION_SERVLET_NAME).servletContext(servletContext).build();
    var servletConfig = ProxyFactories.createServletConfigProxy(servletConfigInvocationHandler);

    var requestInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE).requestUri(REQUEST_URI)
        .servletContext(servletContext).build();
    var httpServletRequest = ProxyFactories.createHttpServletRequestProxy(requestInvocationHandler);

    var responseInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var httpServletResponse =
        ProxyFactories.createHttpServletResponseProxy(responseInvocationHandler);

    // Set up Servlet for run.
    var sessionServlet = new SessionServlet();

    try {

      // This is a call Tomcat has to do; we do it here to simulate that.
      sessionServlet.init(servletConfig);

      // Execute the request.
      sessionServlet.service(httpServletRequest, httpServletResponse);

    } catch (Exception e) {
      fail("Unexpected error!", e);
      if (e.getCause() != null) {
        e.getCause().printStackTrace();
      }
    }

    // Asserts
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseInvocationHandler.getStatusCode());
    assertTrue(responseInvocationHandler.sendErrorWasCalled(),
        "HttpServletResponse.sendError(int, String) was not called!");

    // assert session key cookie is not set
    Optional<Cookie> maybeHaveSessionKeyCookie =
        responseInvocationHandler.getResponseCookiesView().stream()
            .filter(c -> c.getName().equals(ServletConstants.SESSION_KEY_COOKIE_NAME)).findAny();
    assertFalse(maybeHaveSessionKeyCookie.isPresent(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " cookie value set!");
    assertFalse(responseInvocationHandler.sendRedirectWasCalled(),
        "HttpServletResponse.sendRedirect(String) was called!");
  }

  @Test
  void testInternalErrorStatusOnCreateSessionWhenNoSessionDataObjectPresent() {
    var servletContextInvocationHandler = new ServletApiInvocationHandler.Builder().addAttributes(
        Map.of(ServletConstants.ATTRIBUTES_CONTEXT_PARAM_NAME, SHIBBOLETH_ATTRIBUTE_NAMES_IN_MAP))
        .contextPath(CONTEXT_PATH).build();
    var servletContext = ProxyFactories.createServletContextProxy(servletContextInvocationHandler);

    var servletConfigInvocationHandler = new ServletApiInvocationHandler.Builder()
        .servletName(SESSION_SERVLET_NAME).servletContext(servletContext).build();
    var servletConfig = ProxyFactories.createServletConfigProxy(servletConfigInvocationHandler);

    var requestInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE).requestUri(REQUEST_URI)
        .cookies(List.of(new Cookie(ServletConstants.SERVICE_URL_COOKIE_NAME, SERVICE_URL)))
        .servletContext(servletContext).build();
    var httpServletRequest = ProxyFactories.createHttpServletRequestProxy(requestInvocationHandler);

    var responseInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var httpServletResponse =
        ProxyFactories.createHttpServletResponseProxy(responseInvocationHandler);

    // Set up Servlet for run.
    var sessionServlet = new SessionServlet();

    try {

      // This is a call Tomcat has to do; we do it here to simulate that.
      sessionServlet.init(servletConfig);

      // Execute the request.
      sessionServlet.service(httpServletRequest, httpServletResponse);

    } catch (Exception e) {
      fail("Unexpected error!", e);
      if (e.getCause() != null) {
        e.getCause().printStackTrace();
      }
    }

    assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        responseInvocationHandler.getStatusCode(),
        "HTTP status was not " + HttpServletResponse.SC_INTERNAL_SERVER_ERROR + "!");

    // assert session key cookie is not set
    Optional<Cookie> maybeHaveSessionKeyCookie =
        responseInvocationHandler.getResponseCookiesView().stream()
            .filter(c -> c.getName().equals(ServletConstants.SESSION_KEY_COOKIE_NAME)).findAny();
    assertFalse(maybeHaveSessionKeyCookie.isPresent(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " cookie set!");
    assertFalse(responseInvocationHandler.sendRedirectWasCalled(),
        "HttpServletResponse.sendRedirect(String) was called!");
  }

  @Test
  void testLogoutSession() {

    // Generate a key with canned data; need to check that the servlet did it's job on logout by
    // making sure session data was destroyed.
    String keyToDestroy = new String(sessionData.save(CANNED_SESSION_DATA));

    var servletContextInvocationHandler =
        new ServletApiInvocationHandler.Builder().addAttributes(SERVLET_CONTEXT_ATTRIBUTES)
            .addInitParameters(SERVLET_CONTEXT_INIT_PARAMETERS).contextPath(CONTEXT_PATH).build();
    var servletContext = ProxyFactories.createServletContextProxy(servletContextInvocationHandler);

    var servletConfigInvocationHandler = new ServletApiInvocationHandler.Builder()
        .servletName(SESSION_SERVLET_NAME).servletContext(servletContext).build();
    var servletConfig = ProxyFactories.createServletConfigProxy(servletConfigInvocationHandler);

    var requestInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE)
        .cookies(List.of(new Cookie(ServletConstants.SESSION_KEY_COOKIE_NAME, keyToDestroy),
            new Cookie(ServletConstants.SERVICE_URL_COOKIE_NAME, SERVICE_URL)))
        .addRequestParameters(LOGOUT_REQUEST_PARAMETERS).requestUri(REQUEST_URI)
        .servletContext(servletContext).build();
    var httpServletRequest = ProxyFactories.createHttpServletRequestProxy(requestInvocationHandler);

    var responseInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var httpServletResponse =
        ProxyFactories.createHttpServletResponseProxy(responseInvocationHandler);

    // Set up Servlet for run.
    var sessionServlet = new SessionServlet();

    try {

      // This is a call Tomcat has to do; we do it here to simulate that.
      sessionServlet.init(servletConfig);

      // Execute the request.
      sessionServlet.service(httpServletRequest, httpServletResponse);

    } catch (Exception e) {
      fail("Unexpected error!", e);
      if (e.getCause() != null) {
        e.getCause().printStackTrace();
      }
    }

    // Asserts
    assertEquals(HttpServletResponse.SC_FOUND, responseInvocationHandler.getStatusCode());

    // assert session key cookie is unset
    Optional<Cookie> maybeHaveSessionKeyCookie =
        responseInvocationHandler.getResponseCookiesView().stream()
            .filter(c -> c.getName().equals(ServletConstants.SESSION_KEY_COOKIE_NAME)).findAny();
    assertTrue(maybeHaveSessionKeyCookie.isPresent(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " cookie not present on destroy!");
    assertNull(maybeHaveSessionKeyCookie.get().getValue(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " cookie value set on destroy!");
    assertEquals(0, maybeHaveSessionKeyCookie.get().getMaxAge(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " has non-zero max age on destroy!");

    // assert service url cookie is unset
    Optional<Cookie> maybeHaveServiceUrlCookie =
        responseInvocationHandler.getResponseCookiesView().stream()
            .filter(c -> c.getName().equals(ServletConstants.SERVICE_URL_COOKIE_NAME)).findAny();
    assertTrue(maybeHaveServiceUrlCookie.isPresent(),
        ServletConstants.SERVICE_URL_COOKIE_NAME + " cookie not present on destroy!");
    assertNull(maybeHaveServiceUrlCookie.get().getValue(),
        ServletConstants.SERVICE_URL_COOKIE_NAME + " cookie value set on destroy!");
    assertEquals(0, maybeHaveServiceUrlCookie.get().getMaxAge(),
        ServletConstants.SERVICE_URL_COOKIE_NAME + " has non-zero max age on destroy!");

    assertTrue(responseInvocationHandler.sendRedirectWasCalled(),
        "HttpServletResponse.sendRedirect(String) was not called!");
    assertEquals(LOGOUT_URL, responseInvocationHandler.getRedirect(),
        "Redirect was not set to " + LOGOUT_URL + "!");

    // Session data was destroyed
    assertNull(sessionData.get(keyToDestroy.getBytes()));
  }

  @Test
  void testLogoutSessionNoLogoutUrlSet() {

    // Generate a key with canned data; need to check that the servlet did it's job on logout by
    // making sure session data was destroyed.
    String keyToDestroy = new String(sessionData.save(CANNED_SESSION_DATA));

    var servletContextInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(SERVLET_CONTEXT_ATTRIBUTES).contextPath(CONTEXT_PATH).build();
    var servletContext = ProxyFactories.createServletContextProxy(servletContextInvocationHandler);

    var servletConfigInvocationHandler = new ServletApiInvocationHandler.Builder()
        .servletName(SESSION_SERVLET_NAME).servletContext(servletContext).build();
    var servletConfig = ProxyFactories.createServletConfigProxy(servletConfigInvocationHandler);

    var requestInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(SHIBBOLETH_ATTRIBUTES_REQUEST_ATTRIBUTE)
        .cookies(List.of(new Cookie(ServletConstants.SESSION_KEY_COOKIE_NAME, keyToDestroy),
            new Cookie(ServletConstants.SERVICE_URL_COOKIE_NAME, SERVICE_URL)))
        .addRequestParameters(LOGOUT_REQUEST_PARAMETERS).requestUri(REQUEST_URI)
        .servletContext(servletContext).build();
    var httpServletRequest = ProxyFactories.createHttpServletRequestProxy(requestInvocationHandler);

    var responseInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var httpServletResponse =
        ProxyFactories.createHttpServletResponseProxy(responseInvocationHandler);

    // Set up Servlet for run.
    var sessionServlet = new SessionServlet();

    try {

      // This is a call Tomcat has to do; we do it here to simulate that.
      sessionServlet.init(servletConfig);

      // Execute the request.
      sessionServlet.service(httpServletRequest, httpServletResponse);

    } catch (Exception e) {
      fail("Unexpected error!", e);
      if (e.getCause() != null) {
        e.getCause().printStackTrace();
      }
    }

    // Asserts
    assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        responseInvocationHandler.getStatusCode());

    // assert session key cookie is unset
    Optional<Cookie> maybeHaveSessionKeyCookie =
        responseInvocationHandler.getResponseCookiesView().stream()
            .filter(c -> c.getName().equals(ServletConstants.SESSION_KEY_COOKIE_NAME)).findAny();
    assertTrue(maybeHaveSessionKeyCookie.isPresent(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " cookie not present on destroy!");
    assertNull(maybeHaveSessionKeyCookie.get().getValue(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " cookie value set on destroy!");
    assertEquals(0, maybeHaveSessionKeyCookie.get().getMaxAge(),
        ServletConstants.SESSION_KEY_COOKIE_NAME + " has non-zero max age on destroy!");

    // assert service url cookie is unset
    Optional<Cookie> maybeHaveServiceUrlCookie =
        responseInvocationHandler.getResponseCookiesView().stream()
            .filter(c -> c.getName().equals(ServletConstants.SERVICE_URL_COOKIE_NAME)).findAny();
    assertTrue(maybeHaveServiceUrlCookie.isPresent(),
        ServletConstants.SERVICE_URL_COOKIE_NAME + " cookie not present on destroy!");
    assertNull(maybeHaveServiceUrlCookie.get().getValue(),
        ServletConstants.SERVICE_URL_COOKIE_NAME + " cookie value set on destroy!");
    assertEquals(0, maybeHaveServiceUrlCookie.get().getMaxAge(),
        ServletConstants.SERVICE_URL_COOKIE_NAME + " has non-zero max age on destroy!");

    assertFalse(responseInvocationHandler.sendRedirectWasCalled(),
        "HttpServletResponse.sendRedirect(String) was called!");
    assertNotEquals(LOGOUT_URL, responseInvocationHandler.getRedirect(),
        "Redirect was set to " + LOGOUT_URL + "!");

    // Session data was destroyed
    assertNull(sessionData.get(keyToDestroy.getBytes()));
  }
}
