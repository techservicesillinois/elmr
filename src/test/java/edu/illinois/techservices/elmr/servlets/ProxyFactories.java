package edu.illinois.techservices.elmr.servlets;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

final class ProxyFactories {

  private ProxyFactories() {
    // empty constructor prevents instantiation and extension.
  }

  static ServletConfig createServletConfigProxy(InvocationHandler ih) {
    return (ServletConfig) Proxy.newProxyInstance(ProxyFactories.class.getClassLoader(),
        new Class<?>[] {ServletConfig.class}, ih);
  }

  static ServletContext createServletContextProxy(InvocationHandler ih) {
    return (ServletContext) Proxy.newProxyInstance(ProxyFactories.class.getClassLoader(),
        new Class<?>[] {ServletContext.class}, ih);
  }

  static FilterConfig createFilterConfigProxy(InvocationHandler ih) {
    return (FilterConfig) Proxy.newProxyInstance(ProxyFactories.class.getClassLoader(),
        new Class<?>[] {FilterConfig.class}, ih);
  }

  static FilterChain createFilterChainProxy(InvocationHandler ih) {
    return (FilterChain) Proxy.newProxyInstance(ProxyFactories.class.getClassLoader(),
        new Class<?>[] {FilterChain.class}, ih);
  }

  static HttpServletRequest createHttpServletRequestProxy(InvocationHandler ih) {
    return (HttpServletRequest) Proxy.newProxyInstance(ProxyFactories.class.getClassLoader(),
        new Class<?>[] {HttpServletRequest.class}, ih);
  }

  static HttpServletResponse createHttpServletResponseProxy(InvocationHandler ih) {
    return (HttpServletResponse) Proxy.newProxyInstance(ProxyFactories.class.getClassLoader(),
        new Class<?>[] {HttpServletResponse.class}, ih);
  }
}
