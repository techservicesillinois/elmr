package edu.illinois.techservices.elmr.servlets;

import java.io.IOException;
import java.util.TreeSet;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that will return a page listing the Apache, Tomcat and Shibboleth configuration.
 * 
 * <p>
 * This is a diagnostics servlet and is not intended for production use. It should be protected in
 * such a way that only administrators can get to it. The page shows only the attributes configured;
 * no user specific information is displayed.
 * 
 * <p>
 * This servlet only responds to a GET request. All other methods will respond with a
 * {@link HttpServletResponse#SC_METHOD_NOT_ALLOWED Method Not Allowed} status.
 */
@WebServlet("/config")
public class ConfigServlet extends HttpServlet {

  private static final long serialVersionUID = 6843101899645906297L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    var apacheConfig = (ApacheConfig) getServletContext()
        .getAttribute(ServletConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME);

    if (apacheConfig == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Apache configuration was not found!");
      return;
    }

    @SuppressWarnings("unchecked")
    List<String> userAttributes = (List<String>) getServletContext()
        .getAttribute(ServletConstants.ATTRIBUTES_CONTEXT_PARAM_NAME);
    var jkEnvVars = apacheConfig.getJkEnvVars();

    var allAttrs = new TreeSet<String>();
    allAttrs.addAll(userAttributes);
    allAttrs.addAll(jkEnvVars);

    var logoutUrl = getServletContext().getContextPath() + "/session?mode=logout";
    var html = HtmlSupport.renderConfigPage(logoutUrl, allAttrs, apacheConfig.getJkEnvVars(), userAttributes);
    response.setContentType("text/html; charset=UTF-8");
    response.setContentLength(html.length);
    try (var os = response.getOutputStream()) {
      os.write(html);
    }
  }
}
