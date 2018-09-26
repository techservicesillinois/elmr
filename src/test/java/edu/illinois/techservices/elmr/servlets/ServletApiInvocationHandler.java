package edu.illinois.techservices.elmr.servlets;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.MethodNotFoundException;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;

/**
 * InvocationHandler that can be used by Proxy objects that implement interfaces in the
 * {@link javax.servlet.http} package.
 * 
 * <p>
 * This implementation can invoke methods for the following interfaces:
 * 
 * <ul>
 * <li>{@link javax.servlet.FilterConfig}
 * <li>{@link javax.servlet.FilterChain}
 * <li>{@link javax.servlet.ServletConfig}
 * <li>{@link javax.servlet.ServletContext}
 * <li>{@link javax.servlet.http.HttpServletRequest}
 * <li>{@link javax.servlet.http.HttpServletResponse}
 * </ul>
 * 
 * <p>
 * This implementation does not 100% cover all methods in the servlet API. Rather, it is an
 * invocation handler of just the methods invoked for the proxy objects in the tests. No interface
 * is completely implemented by this class.
 */
class ServletApiInvocationHandler implements InvocationHandler {

  private static final int SC_FOUND = 302;

  private final List<Cookie> cookies = new ArrayList<>();

  private final List<Cookie> responseCookies = new ArrayList<>();

  private final Map<String, Object> attributes = new HashMap<>();

  private final Map<String, String> initParameters = new HashMap<>();

  private final Map<String, List<String>> requestParameters = new HashMap<>();

  private final ServletContext servletContext;

  private final String contextPath;

  private final String filterName;

  private final String requestUri;

  private final String servletName;

  private int addCookieCallCount = 0;

  private int doFilterCallCount = 0;

  private int sendErrorCallCount = 0;

  private int sendRedirectCallCount = 0;

  private int setAttributeCallCount = 0;

  private int setStatusCallCount = 0;

  private int statusCode = -1;

  private String redirect = "";

  /**
   * Builder for instances of this InvocationHandler.
   * 
   * <p>
   * The objects in the Servlet API all use similarly named methods and have similar needs, but
   * there's not 100% overlap. The Builder avoids a ridiculous number of telescoping constructors in
   * that it is called passing those parameters needed by a particular instance of this
   * InvocationHandler.
   */
  static final class Builder {

    private final List<Cookie> cookies = new ArrayList<>();

    private final Map<String, Object> attributes = new HashMap<>();

    private final Map<String, String> initParameters = new HashMap<>();

    private final Map<String, List<String>> requestParameters = new HashMap<>();

    private ServletContext servletContext = null;

    private String contextPath = null;

    private String filterName = "";

    private String requestUri = "";

    private String servletName = "";

    Builder addAttributes(Map<String, Object> attributes) {
      this.attributes.putAll(attributes);
      return this;
    }

    Builder addInitParameters(Map<String, String> initParameters) {
      this.initParameters.putAll(initParameters);
      return this;
    }

    Builder addRequestParameters(Map<String, List<String>> requestParameters) {
      this.requestParameters.putAll(requestParameters);
      return this;
    }

    Builder contextPath(String contextPath) {
      this.contextPath = contextPath;
      return this;
    }

    Builder cookies(Cookie[] cookies) {
      this.cookies.addAll(Arrays.asList(cookies));
      return this;
    }

    Builder cookies(Collection<Cookie> cookies) {
      this.cookies.addAll(cookies);
      return this;
    }

    Builder filterName(String filterName) {
      this.filterName = filterName;
      return this;
    }

    Builder requestUri(String requestUri) {
      this.requestUri = requestUri;
      return this;
    }

    Builder servletContext(ServletContext servletContext) {
      this.servletContext = servletContext;
      return this;
    }

    Builder servletName(String servletName) {
      this.servletName = servletName;
      return this;
    }

    ServletApiInvocationHandler build() {
      return new ServletApiInvocationHandler(this);
    }
  }

  /**
   * Constructs a new instance.
   * 
   * @param builder the Builder.
   */
  private ServletApiInvocationHandler(Builder builder) {
    this.cookies.addAll(builder.cookies);
    this.attributes.putAll(builder.attributes);
    this.initParameters.putAll(builder.initParameters);
    this.requestParameters.putAll(builder.requestParameters);
    this.servletContext = builder.servletContext;
    this.contextPath = builder.contextPath;
    this.filterName = builder.filterName;
    this.requestUri = builder.requestUri;
    this.servletName = builder.servletName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    if (method.getName().equals("addCookie")) {

      addCookieCallCount++;
      responseCookies.add((Cookie) args[0]);
      return null;

    } else if (method.getName().equals("doFilter")) {

      doFilterCallCount++;
      return null;

    } else if (method.getName().equals("getAttribute")) {

      String name = args[0].toString();
      return attributes.get(name);

    } else if (method.getName().equals("getContextPath")) {

      // Following the API specification for ServletContext, with the assumption that if the path
      // isn't set, assume it's the ROOT context (return a "").
      return (contextPath == null || contextPath.isEmpty()) ? ""
          : contextPath.startsWith("/") ? contextPath : "/" + contextPath;

    } else if (method.getName().equals("getCookies")) {

      Cookie[] cookiesArray = new Cookie[cookies.size()];
      return cookies.toArray(cookiesArray);

    } else if (method.getName().equals("getFilterName")) {

      return filterName;

    } else if (method.getName().equals("getInitParameter")) {

      return initParameters.get(args[0].toString());

    } else if (method.getName().equals("getInitParameterNames")) {

      return new NamesEnumeration(initParameters.keySet().iterator());

    } else if (method.getName().equals("getParameter")) {

      List<String> values = requestParameters.get(args[0].toString());
      if (values != null) {
        return values.get(0);
      } else {
        return null;
      }

    } else if (method.getName().equals("getRequestURI")) {

      return requestUri;

    } else if (method.getName().equals("getServletContext")) {

      return servletContext;

    } else if (method.getName().equals("getServletName")) {

      return servletName;

    } else if (method.getName().equals("sendError")) {

      sendErrorCallCount++;
      statusCode = Integer.valueOf(args[0].toString());
      return null;

    } else if (method.getName().equals("sendRedirect")) {

      sendRedirectCallCount++;
      @SuppressWarnings("unused")
      URL location = null;
      try {
        location = new URL("http://localhost:8080" + args[0].toString());
      } catch (MalformedURLException e) {
        throw new IllegalStateException(e);
      }
      redirect = args[0].toString();
      statusCode = SC_FOUND;
      return null;

    } else if (method.getName().equals("setAttribute")) {

      setAttributeCallCount++;
      attributes.put(args[0].toString(), args[1]);
      return null;

    } else if (method.getName().equals("setStatus")) {

      setStatusCallCount++;
      statusCode = Integer.valueOf(args[0].toString());
      return null;

    } else {

      throw new MethodNotFoundException("Don't know about " + method.getName());

    }
  }

  /**
   * Returns {@code true} if {@link javax.servlet.http.HttpServletResponse#addCookie(Cookie)
   * addCookie} was called at least once.
   */
  boolean addCookieWasCalled() {
    return addCookieCallCount > 0;
  }

  /**
   * Returns the number of times {@link javax.servlet.http.HttpServletResponse#addCookie addCookie}
   * was called.
   */
  int getAddCookieCallCount() {
    return addCookieCallCount;
  }

  /**
   * Returns an unmodifiable view of the attributes.
   */
  Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  /**
   * Returns {@code true} if
   * {@link javax.servlet.GenericFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   * doFilter} was called at least once.
   */
  boolean doFilterWasCalled() {
    return doFilterCallCount > 0;
  }

  /**
   * Returns the number of times
   * {@link javax.servlet.GenericFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   * doFilter} was called.
   */
  int getDoFilterCallCount() {
    return doFilterCallCount;
  }

  /**
   * Returns {@code true} if {@code setAttribute} was called at least once.
   * 
   * <p>
   * There is a {@code setAttribute} method in {@link ServletContext},
   * {@link javax.servlet.http.HttpServletRequest HttpServletRequest}, and
   * {@link javax.servlet.http.HttpSession HttpSession}.
   */
  boolean setAttributeWasCalled() {
    return setAttributeCallCount > 0;
  }

  int getSetAttributeCallCount() {
    return setAttributeCallCount;
  }

  /**
   * Returns the redirect url if {@link javax.servlet.http.HttpServletResponse#sendRedirect(String)
   * sendRedirect} is called or an empty String.
   */
  String getRedirect() {
    return redirect;
  }

  /**
   * Returns the List of Cookies set on the response or an empty List if none set.
   */
  List<Cookie> getResponseCookiesView() {
    return Collections.unmodifiableList(responseCookies);
  }

  /**
   * Returns {@code true} if {@link javax.servlet.http.HttpServletResponse#sendError(int, String)
   * sendError} was called at least once.
   */
  boolean sendErrorWasCalled() {
    return sendErrorCallCount > 0;
  }

  /**
   * Returns the number of times
   * {@link javax.servlet.http.HttpServletResponse#sendError(int, String) sendError} was called.
   */
  int getSendErrorCallCount() {
    return sendErrorCallCount;
  }

  /**
   * Returns {@code true} if {@link javax.servlet.http.HttpServletResponse#sendRedirect(String)
   * sendRedirect} was called at least once.
   */
  boolean sendRedirectWasCalled() {
    return sendRedirectCallCount > 0;
  }

  /**
   * Returns the number of times {@link javax.servlet.http.HttpServletResponse#sendRedirect(String)
   * sendRedirect} was called.
   */
  int getSendRedirectCallCount() {
    return sendRedirectCallCount;
  }

  /**
   * Returns {@code true} if {@link javax.servlet.http.HttpServletResponse#setStatus(int) setStatus}
   * was called at least once.
   */
  boolean setStatusWasCalled() {
    return setStatusCallCount > 0;
  }

  /**
   * Returns the number of times {@link javax.servlet.http.HttpServletResponse#setStatus(int)
   * setStatus} was called.
   */
  int getSetStatusCallCount() {
    return setStatusCallCount;
  }

  /**
   * Returns the status code set by {@link javax.servlet.http.HttpServletResponse#setStatus(int)
   * HttpServletResponse.setStatus} or {@code -1} if not set.
   */
  int getStatusCode() {
    return statusCode;
  }

  /**
   * Wraps an {@link Iterator} in an {@link Enumeration} to conform to various Servlet API.
   */
  private static class NamesEnumeration implements Enumeration<String> {

    private final Iterator<String> initParameterNames;

    /**
     * Constructs a new instance.
     * 
     * @param names Iterator of the names.
     */
    private NamesEnumeration(Iterator<String> names) {
      this.initParameterNames = names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMoreElements() {
      return initParameterNames.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String nextElement() {
      return initParameterNames.next();
    }
  }
}
