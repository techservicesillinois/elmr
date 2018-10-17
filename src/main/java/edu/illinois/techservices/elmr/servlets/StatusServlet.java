package edu.illinois.techservices.elmr.servlets;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.illinois.techservices.elmr.Json;
import edu.illinois.techservices.elmr.SessionData;

/**
 * Servlet that can be used as a status/health check for elmr.
 */
@WebServlet("/status")
public class StatusServlet extends HttpServlet {

  /**
   * Check the status of a connection to the session data store.
   * 
   * <p>
   * The possible statuses from a call to this method are:
   * 
   * <dl>
   * <dt>{@value HttpServletResponse#SC_NO_CONTENT}
   * <dd><strong>SUCCESS</strong>: Connection is available and working.
   * <dt>{@value HttpServletResponse#SC_INTERNAL_SERVER_ERROR}
   * <dd><strong>FAILURE</strong>: Web application cannot find an active object representing a
   * connection to the session data store. Usually means the application was reconfigured and the
   * session data object was not instantiated and set as an attribute in the servlet context.
   * <dt>{@value HttpServletResponse#SC_SERVICE_UNAVAILABLE}
   * <dd><strong>FAILURE</strong>: Session data object was found, but there is no connection to the
   * actual store.
   * </dl>
   * 
   * <p>
   * A json payload will be returned if there was a failure as previously described containing the
   * HTTP status code and a message explaining what the method could determine. An example response
   * looks like:
   * 
   * <pre>{@code 
   * 
   * HTTP/1.1 503 Service Unavailable 
   * Content-Length: 76 
   * Content-Type: application/json
   * 
   * { 
   *   "httpStatus": 503, 
   *   "msg": "Could not connect to session data store."
   * }
   * 
   * }</pre>
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    var sd = (SessionData) getServletContext()
        .getAttribute(ServletConstants.SESSION_DATA_CONTEXT_PARAM_NAME);
    var sc = HttpServletResponse.SC_NO_CONTENT;
    var msg = "";
    if (sd == null) {
      sc = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      msg = "Session data store not set in web application.";
    } else if (!sd.isConnected()) {
      sc = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
      msg = "Could not connect to session data store.";
    }

    if (isSuccessStatus(sc)) {
      response.setStatus(sc);
    } else {
      var json = Json.renderObject(generateErrorResponseObject(sc, msg));
      response.setContentLength(json.length());
      response.setContentType("application/json");
      response.setStatus(sc);
      var pw = response.getWriter();
      pw.print(json);
      pw.flush();
    }
    return;
  }

  private Map<String, Object> generateErrorResponseObject(int statusCode, String message) {
    return Map.of("httpStatus", statusCode, "message", message);
  }

  private boolean isSuccessStatus(int sc) {
    return sc >= 200 && sc <= 299;
  }
}
