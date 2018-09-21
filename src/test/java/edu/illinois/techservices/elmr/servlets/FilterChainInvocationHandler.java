package edu.illinois.techservices.elmr.servlets;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.el.MethodNotFoundException;

/**
 * InvocationHandler for {@link javax.servlet.FilterChain} objects used in tests.
 */
public class FilterChainInvocationHandler implements InvocationHandler {

  private boolean doFilterCalled = false;

  /**
   * {@inheritDoc}
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getName().equals("doFilter")) {
      doFilterCalled = true;
      return null;
    } else {
      throw new MethodNotFoundException("Don't know about " + method.getName());
    }
  }

  boolean doFilterCalled() {
    return doFilterCalled;
  }
}
