package edu.illinois.techservices.elmr.servlets;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.illinois.techservices.elmr.Json;
import edu.illinois.techservices.elmr.SessionData;

/**
 * WebFilter that is a companion to {@link AttributesServlet} that will redirect
 */
@WebFilter(servletNames = {"AttributesServlet"})
public class AttributesFilter extends HttpFilter {

  private static final Logger LOGGER = Logger.getLogger(AttributesFilter.class.getName());

  private static final long serialVersionUID = -4430111522056714684L;

  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    var cookies = req.getCookies();
    var sessionKeyCookieFound = false;
    var encodedKey = "";
    if (cookies != null) {
      for (int i = 0; i < cookies.length; i++) {
        Cookie c = cookies[i];
        if (c.getName().equals(PackageConstants.SESSION_KEY_COOKIE_NAME)) {
          sessionKeyCookieFound = true;
          encodedKey = c.getValue();
          break;
        }
      }
    }

    if (!sessionKeyCookieFound) {
      LOGGER.fine("Did not find the session key, redirecting to create a session.");
      var serviceUrlCookie = new Cookie(PackageConstants.SERVICE_URL_COOKIE_NAME, req.getRequestURI());
      serviceUrlCookie.setPath("/");
      res.addCookie(serviceUrlCookie);
      res.sendRedirect(getServletContext().getContextPath() + "/session");
      return;
    } else {
      // This filter will take advantage of the fact that it's part of elmr and use the SessionData
      // implementation to get its session data.
      LOGGER.fine("Found session key " + encodedKey);
      var sd = (SessionData) getServletContext()
          .getAttribute(SessionDataContextListener.class.getPackageName() + ".sessionData");
      var sessionDataJson = sd.get(encodedKey.getBytes());
      var sessionData = Json.marshal(sessionDataJson);
      if (sessionData != null) {
        sessionData.keySet().stream().forEach(k -> req.setAttribute(k, sessionData.get(k)));
      }
      chain.doFilter(req, res);
    }
  }
}
