package edu.illinois.techservices.elmr.servlets;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.el.MethodNotFoundException;

/**
 * Proxy for ServletContext objects used in tests.
 */
public class ServletContextProxy implements InvocationHandler {

  private final Map<String, Object> initParameters;

  private final Map<String, Object> attributes = new HashMap<>();

  public ServletContextProxy(Map<String, Object> initParameters) {
    this.initParameters = Collections.unmodifiableMap(initParameters);
  }

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
