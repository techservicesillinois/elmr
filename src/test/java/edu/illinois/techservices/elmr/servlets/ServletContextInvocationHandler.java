package edu.illinois.techservices.elmr.servlets;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.el.MethodNotFoundException;

/**
 * InvocationHandler for {@link javax.servlet.ServletContext} objects used in tests.
 */
public class ServletContextInvocationHandler implements InvocationHandler {

  private final Map<String, Object> initParameters;

  private final Map<String, Object> attributes = new HashMap<>();

  /**
   * Constructs a new instance.
   * 
   * @param initParameters a Map of the context initialization parameters.
   */
  public ServletContextInvocationHandler(Map<String, Object> initParameters) {
    this.initParameters = Collections.unmodifiableMap(initParameters);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getName().equals("getInitParameter")) {
      String name = args[0].toString();
      return initParameters.get(name);
    } else if (method.getName().equals("setAttribute")) {
      String name = args[0].toString();
      Object value = args[1];
      attributes.put(name, value);
      return null;
    } else if (method.getName().equals("getAttribute")) {
      String name = args[0].toString();
      return attributes.get(name);
    } else {
      throw new MethodNotFoundException("Don't know about " + method.getName());
    }
  }
}
