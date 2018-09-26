package edu.illinois.techservices.elmr.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import edu.illinois.techservices.elmr.InMemorySessionData;

class AttributesFilterTest {

  private static final String REDIRECT_URI = "/elmr/session";

  private static final String REQUEST_URI = "/elmr/attributes";

  private static final String CONTEXT_PATH = "/elmr";

  private static final String FILTER_NAME = "AttributesFilter";

  private static final String TEST_DATA = "{\"displayName\": \"for-test\",\"uid\": \"abcd1234\"}";

  private static final Map<String, Object> EXPECTED_ATTRIBUTES =
      Map.of("displayName", "for-test", "uid", "abcd1234");

  @Test
  void testNoSessionKeyCookieSet() {

    // Set up the servlet filter.
    var servletContextInvocationHandler =
        new ServletApiInvocationHandler.Builder().contextPath(CONTEXT_PATH).build();
    var servletContext = ProxyFactories.createServletContextProxy(servletContextInvocationHandler);
    var filterConfigInvocationHandler = new ServletApiInvocationHandler.Builder()
        .filterName(FILTER_NAME).servletContext(servletContext).build();
    var filterConfig = ProxyFactories.createFilterConfigProxy(filterConfigInvocationHandler);

    // Set up the request.
    var requestInvocationHandler =
        new ServletApiInvocationHandler.Builder().requestUri(REQUEST_URI).build();
    var request = ProxyFactories.createHttpServletRequestProxy(requestInvocationHandler);
    var responseInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var response = ProxyFactories.createHttpServletResponseProxy(responseInvocationHandler);
    var filterChainInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var chain = ProxyFactories.createFilterChainProxy(filterChainInvocationHandler);

    var attributesFilter = new AttributesFilter();
    try {

      // This is a call Tomcat has to do; we do it here to simulate that.
      attributesFilter.init(filterConfig);

      // Execute the request.
      attributesFilter.doFilter(request, response, chain);

    } catch (Exception e) {
      fail("Test error!", e);
    }

    // Asserts here.
    var cookies = responseInvocationHandler.getResponseCookiesView();
    Optional<Cookie> maybeHaveServiceUrlCookie = cookies.stream()
        .filter(c -> c.getName().equals(PackageConstants.SERVICE_URL_COOKIE_NAME)).findAny();
    assertTrue(maybeHaveServiceUrlCookie.isPresent());
    assertTrue(maybeHaveServiceUrlCookie.get().getValue().equals(REQUEST_URI));

    Optional<Cookie> maybeHaveSessionKeyCookie = cookies.stream()
        .filter(c -> c.getName().equals(PackageConstants.SESSION_KEY_COOKIE_NAME)).findAny();
    assertFalse(maybeHaveSessionKeyCookie.isPresent());

    assertTrue(responseInvocationHandler.getRedirect().equals(REDIRECT_URI));
    assertFalse(filterChainInvocationHandler.doFilterWasCalled());
    assertFalse(servletContextInvocationHandler.setAttributeWasCalled());
  }

  @Test
  void testCookieSetSessionDataSet() {
    var sd = new InMemorySessionData();
    var key = sd.save(TEST_DATA);
    var contextAttributes = new HashMap<String, Object>();
    contextAttributes.put(PackageConstants.SESSION_DATA_CONTEXT_PARAM_NAME, sd);
    var servletContextInvocationHandler =
        new ServletApiInvocationHandler.Builder().addAttributes(contextAttributes).build();
    var servletContext = ProxyFactories.createServletContextProxy(servletContextInvocationHandler);
    var filterConfigInvocationHandler = new ServletApiInvocationHandler.Builder()
        .filterName(FILTER_NAME).servletContext(servletContext).build();
    var filterConfig = ProxyFactories.createFilterConfigProxy(filterConfigInvocationHandler);

    // Set up the request.
    var requestInvocationHandler = new ServletApiInvocationHandler.Builder().requestUri(REQUEST_URI)
        .cookies(List.of(new Cookie(PackageConstants.SESSION_KEY_COOKIE_NAME, new String(key))))
        .build();
    var request = ProxyFactories.createHttpServletRequestProxy(requestInvocationHandler);
    var responseInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var response = ProxyFactories.createHttpServletResponseProxy(responseInvocationHandler);
    var filterChainInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var chain = ProxyFactories.createFilterChainProxy(filterChainInvocationHandler);

    var attributesFilter = new AttributesFilter();
    try {

      // This is a call Tomcat has to do; we do it here to simulate that.
      attributesFilter.init(filterConfig);

      // Execute the request.
      attributesFilter.doFilter(request, response, chain);

    } catch (Exception e) {
      fail("Test error!", e);
    }

    // Asserts here.
    var cookies = responseInvocationHandler.getResponseCookiesView();
    Optional<Cookie> maybeHaveServiceUrlCookie = cookies.stream()
        .filter(c -> c.getName().equals(PackageConstants.SERVICE_URL_COOKIE_NAME)).findAny();
    assertFalse(maybeHaveServiceUrlCookie.isPresent());

    var attributes = requestInvocationHandler.getAttributes();
    assertEquals(EXPECTED_ATTRIBUTES.size(), attributes.size());
    for (String attributeName : EXPECTED_ATTRIBUTES.keySet()) {
      assertTrue(attributes.containsKey(attributeName));
      assertTrue(attributes.get(attributeName).equals(EXPECTED_ATTRIBUTES.get(attributeName)));
    }
    assertTrue(filterChainInvocationHandler.doFilterWasCalled());
  }
  @Test
  void testNoSessionDataObjectOnContext() {

    // Set up a ServletContext with empty attributes. This will force the NPE.
    var servletContextInvocationHandler = new ServletApiInvocationHandler.Builder()
        .addAttributes(new HashMap<String, Object>()).build();
    var servletContext = ProxyFactories.createServletContextProxy(servletContextInvocationHandler);
    var filterConfigInvocationHandler = new ServletApiInvocationHandler.Builder()
        .filterName(FILTER_NAME).servletContext(servletContext).build();
    var filterConfig = ProxyFactories.createFilterConfigProxy(filterConfigInvocationHandler);

    // Set up the request.
    var requestInvocationHandler = new ServletApiInvocationHandler.Builder().requestUri(REQUEST_URI)
        .cookies(List.of(new Cookie(PackageConstants.SESSION_KEY_COOKIE_NAME, "not used"))).build();
    var request = ProxyFactories.createHttpServletRequestProxy(requestInvocationHandler);
    var responseInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var response = ProxyFactories.createHttpServletResponseProxy(responseInvocationHandler);
    var filterChainInvocationHandler = new ServletApiInvocationHandler.Builder().build();
    var chain = ProxyFactories.createFilterChainProxy(filterChainInvocationHandler);

    var attributesFilter = new AttributesFilter();
    try {

      // This is a call Tomcat has to do; we do it here to simulate that.
      attributesFilter.init(filterConfig);

      // Execute the request and make sure an NPE is thrown.
      assertThrows(NullPointerException.class,
          () -> attributesFilter.doFilter(request, response, chain));

    } catch (Exception e) {
      fail("Test error!", e);
    }
  }
}
