package edu.illinois.techservices.elmr.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Displays Shibboleth attribute names and values associated with the current user.
 * 
 * <p>
 * <strong>Deployment Note:</strong> the url-pattern associated with this servlet does not have to
 * be Shibboleth-protected. It is a demonstration for using the session data store and therefore
 * does not need direct access to Shibboleth attributes. However, it is a good idea to protect
 * access to it using Shibboleth to ensure regular users don't have access to it (or remove it from
 * the deployment entirely as this is just a sample).
 */
@WebServlet(urlPatterns = {"/attributes"}, name = "AttributesServlet")
public class AttributesServlet extends HttpServlet {

  private static final long serialVersionUID = 6991995966073870635L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    @SuppressWarnings("unchecked")
    var attrNames = (List<String>) getServletContext()
        .getAttribute(ServletConstants.ATTRIBUTES_CONTEXT_PARAM_NAME);

    Map<String, Object> reqAttrs = new HashMap<>();
    for (Iterator<String> reqAttrNames = req.getAttributeNames().asIterator(); reqAttrNames
        .hasNext();) {
      String reqAttrName = reqAttrNames.next();
      reqAttrs.put(reqAttrName, req.getAttribute(reqAttrName));
    }
    var logoutUrl = getServletContext().getContextPath() + "/session?mode=logout";
    var html = HtmlSupport.renderAttributesPage(logoutUrl, attrNames, reqAttrs);
    resp.setContentType("text/html; charset=UTF-8");
    resp.setContentLength(html.length);
    try (var os = resp.getOutputStream()) {
      os.write(html);
    }
  }
}
