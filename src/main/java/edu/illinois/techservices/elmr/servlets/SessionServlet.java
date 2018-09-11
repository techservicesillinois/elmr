package edu.illinois.techservices.elmr.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.illinois.techservices.elmr.Json;
import edu.illinois.techservices.elmr.SessionData;

/**
 * Servlet that captures attribute data from Shibboleth to store it.
 */
@WebServlet("/session")
public class SessionServlet extends HttpServlet {

  private static final long serialVersionUID = 1755921268489294474L;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    var mode = "create";
    var query = request.getQueryString();
    if (query != null && query.contains("mode=")) {
      String[] params = query.split("=");
      for (int i = 0; i < params.length; i += 2) {
        if (params[i].equals("mode")) {
          mode = params[i + 1].toLowerCase();
        }
      }
    }

    if (mode.equals("destroy")) {
      destroySession(request, response);
    } else {
      createSession(request, response);
    }

    var location = request.getHeader("Location");
    if (location == null) {
      Cookie[] cookies = request.getCookies();
      for (Cookie c : cookies) {
        if (c.getName().equals(PackageConstants.SERVICE_URL_COOKIE_NAME)) {
          location = c.getValue();
          break;
        }
      }
    }
    if (location == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Redirect back to service was not set. "
              + "Set either a Location header or a cookie with the name '"
              + PackageConstants.SERVICE_URL_COOKIE_NAME + "'.");
    } else {
      response.sendRedirect(location);
    }
  }

  private void createSession(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    @SuppressWarnings("unchecked")
    List<String> userAttributes = (List<String>) getServletContext()
        .getAttribute(PackageConstants.ATTRIBUTES_CONTEXT_PARAM_NAME);

    // Generate a Map with attribute names as keys and Shibboleth attribute values as values.
    Map<String, Object> output =
        userAttributes.stream().filter(userAttr -> Objects.nonNull(request.getAttribute(userAttr)))
            .collect(Collectors.toMap(Function.identity(), userAttr -> {
              String attr = (String) request.getAttribute(userAttr);
              if (attr.indexOf(';') > 0) {
                return Arrays.asList(attr.split(";"));
              }
              return attr;
            }));
    if (output.size() <= 0) {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      var sd = (SessionData) getServletContext()
          .getAttribute(PackageConstants.SESSION_DATA_CONTEXT_PARAM_NAME);
      Objects.requireNonNull(sd, "SessionData implementation is null!");
      var json = Json.renderObject(output);
      var key = sd.save(json);
      response.addCookie(new Cookie(PackageConstants.SESSION_KEY_COOKIE_NAME, new String(key)));
    }
  }

  private void destroySession(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    var sd = (SessionData) getServletContext()
        .getAttribute(PackageConstants.SESSION_DATA_CONTEXT_PARAM_NAME);
    Objects.requireNonNull(sd, "SessionData implementation is null!");
    Cookie[] cookies = request.getCookies();
    for (Cookie c : cookies) {
      if (c.getName().equals(PackageConstants.SESSION_KEY_COOKIE_NAME)) {
        byte[] key = c.getValue().getBytes();
        sd.destroy(key);
        break;
      }
    }
  }
}
