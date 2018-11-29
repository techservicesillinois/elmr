package edu.illinois.techservices.elmr.servlets;

import java.io.IOException;
import java.util.List;
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
    @SuppressWarnings("unchecked")
    List<String> userAttributes = (List<String>) getServletContext()
        .getAttribute(ServletConstants.ATTRIBUTES_CONTEXT_PARAM_NAME);

    var log = new StringBuilder("{");
    var sj0 = new StringJoiner(", ");

    userAttributes.stream().forEach(userAttribute -> sj0.add(new StringJoiner(": ")
        .add(userAttribute).add(request.getAttribute(userAttribute).toString()).toString()));

    var uniqueUserId = ElmrParameters.getString(getServletContext(),
        ServletConstants.UNIQUE_USER_ID_PARAM_NAME, ServletConstants.DEFAULT_UNIQUE_USER_ID);
    sj0.add(new StringJoiner(": ").add(uniqueUserId)
        .add(request.getAttribute(uniqueUserId).toString()).toString());
    return log.append(sj0.toString()).append("}").toString();
  }
}
