package edu.illinois.techservices.elmr.servlets;

import java.io.IOException;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter(servletNames = {"SessionServlet"})
public class RequestAttributesLoggingFilter extends HttpFilter {

  private static final long serialVersionUID = -4044203873998829681L;
  private static final Logger LOGGER =
      Logger.getLogger(RequestAttributesLoggingFilter.class.getName());

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Before " + request.getRequestURI() + ": " + logRequestAttributes(request));
    }

    chain.doFilter(request, response);

    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("After " + request.getRequestURI() + ": " + logRequestAttributes(request));
    }

  }

  private String logRequestAttributes(HttpServletRequest request) {
    var attrNames = request.getAttributeNames();
    LOGGER.finest("REMOVE ME attrNames.hasMoreElements() = " + attrNames.hasMoreElements());
    var log = new StringBuilder("{");
    var sj0 = new StringJoiner(", ");
    while (attrNames.hasMoreElements()) {
      var name = attrNames.nextElement();
      var value = request.getAttribute(name).toString();
      sj0.add(new StringJoiner(": ").add(name).add(value).toString());
    }
    return log.append(sj0.toString()).append("}").toString();
  } 
}
