package edu.illinois.techservices.elmr.servlets;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.el.MethodNotFoundException;
import javax.servlet.ServletContext;

/**
 * InvocationHandler for {@link javax.servlet.FilterConfig} objects used in tests.
 */
public class FilterConfigInvocationHandler implements InvocationHandler {

  private final String filtername;

  private final Map<String, String> initParameters = new HashMap<>();

  private final ServletContext servletContext;

  /**
   * Constructs a new instance.
   * 
   * @param servletContext the ServletContext.
   * @param filtername     the filter name.
   * @param initParameters Map of the initialization parameters.
   */
  public FilterConfigInvocationHandler(ServletContext servletContext, String filtername,
      Map<String, String> initParameters) {
    this.servletContext = servletContext;
    this.filtername = filtername;
    this.initParameters.putAll(initParameters);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getName().equals("getFilterName")) {
      return filtername;
    } else if (method.getName().equals("getServletContext")) {
      return servletContext;
    } else if (method.getName().equals("getInitParameter")) {
      String name = args[0].toString();
      return initParameters.get(name);
    } else if (method.getName().equals("getInitParameterNames")) {
      return new InitParameterNamesEnumeration(initParameters.keySet().iterator());
    } else {
      throw new MethodNotFoundException("Don't know about " + method.getName());
    }
  }

  /**
   * Wraps an {@link Iterator} in an {@link Enumeration} to conform to the
   * {@link javax.servlet.FilterConfig} API.
   */
  private static class InitParameterNamesEnumeration implements Enumeration<String> {

    private final Iterator<String> initParameterNames;

    /**
     * Constructs a new instance.
     * 
     * @param initParameterNames Iterator of the initialization parameter names.
     */
    private InitParameterNamesEnumeration(Iterator<String> initParameterNames) {
      this.initParameterNames = initParameterNames;
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
