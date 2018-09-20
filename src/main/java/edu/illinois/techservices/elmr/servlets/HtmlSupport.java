package edu.illinois.techservices.elmr.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utilities for generating Html documents.
 */
class HtmlSupport {

  private static final String CONFIG_PAGE_TITLE = "elmr - Apache Configuration";

  private static final String USER_ATTRIBUTES_TITLE = "elmr - User Attributes";

  private static final String TH = "th";

  private static final String TD = "td";

  private static final String YES = "Yes";

  private static final String NO = "No";

  private HtmlSupport() {
    // Empty constructor prevents instantiation.
  }

  /**
   * Generates and returns an Html page of user attribute values as a byte array.
   * 
   * @param logoutUrl      Url to redirect logout requests to.
   * @param userAttributes Collection of user attribute names.
   * @param reqAttrs       request attributes mapped to the current user's request.
   * @return an Html page of user attributes as a byte array.
   * @throws IOException if an IOException occurs while the page is generated.
   */
  static byte[] renderAttributesPage(String logoutUrl, Collection<String> userAttributes,
      Map<String, Object> reqAttrs) throws IOException {
    try {

      var page = createDocument();

      var table = createTable(page, "table", "table-striped");

      var caption = page.createElement("caption");
      caption.setTextContent("User Attributes");
      table.appendChild(caption);

      var trh = page.createElement("tr");
      trh.appendChild(createTdTh(page, TH, "Name"));
      trh.appendChild(createTdTh(page, TH, "Value", "text-center"));

      var thead = page.createElement("thead");
      thead.appendChild(trh);
      table.appendChild(thead);

      var tbody = page.createElement("tbody");
      for (String userAttribute : userAttributes) {
        var tr = page.createElement("tr");

        tr.appendChild(createTdTh(page, TD, userAttribute));

        // The value of the userAttribute is either a list or a scalar.
        var attrVal = reqAttrs.get(userAttribute);
        if (attrVal instanceof List) {
          @SuppressWarnings("unchecked")
          List<String> attrList = (List<String>) attrVal;
          StringJoiner sj = new StringJoiner(", ");
          for (String attr : attrList) {
            sj.add(attr);
          }
          tr.appendChild(createTdTh(page, TD, sj.toString(), "text-center"));
        } else {
          tr.appendChild(createTdTh(page, TD, attrVal == null ? "no-value" : attrVal.toString(),
              "text-center"));
        }
        tbody.appendChild(tr);
      }
      table.appendChild(tbody);

      var p0 = page.createElement("p");
      p0.setTextContent("The following attributes were found for the current user:");
      p0.appendChild(table);

      var p1 = page.createElement("p");
      p1.appendChild(createLogoutLink(page, logoutUrl, "Logout"));

      var h1 = page.createElement("h1");
      h1.setTextContent(USER_ATTRIBUTES_TITLE);

      var containerDiv0 = createContainerDiv(page);
      containerDiv0.appendChild(h1);
      containerDiv0.appendChild(p0);
      containerDiv0.appendChild(p1);

      var body = page.createElement("body");
      body.appendChild(containerDiv0);

      var html = page.createElement("html");
      html.appendChild(createHtmlHead(page, USER_ATTRIBUTES_TITLE));
      html.appendChild(body);
      page.appendChild(html);

      return renderToByteArray(page);
    } catch (ParserConfigurationException e) {
      throw new AssertionError("Problem with rendering attributes page", e);
    }
  }

  /**
   * Generates and returns an Html page of configuration values as a byte array.
   * 
   * @param logoutUrl          Url to redirect logout requests to.
   * @param allAttributeNames  Collection of all configured attribute names in Apache and
   *                           Shibboleth.
   * @param jkEnvVars          Collection of attribute names exposed as JkEnvVars.
   * @param userAttributeNames Collection of all user attribute names configured in Shibooleth's
   *                           attribute map.
   * @return an Html page of configuration values as a byte array.
   * @throws IOException if an IOException occurs during page generation.
   */
  static byte[] renderConfigPage(String logoutUrl, Collection<String> allAttributeNames,
      Collection<String> jkEnvVars, Collection<String> userAttributeNames) throws IOException {
    try {

      var page = createDocument();

      var table = createTable(page, "table", "table-striped");

      var caption = page.createElement("caption");
      caption.setTextContent("Configured Attribute Names");
      table.appendChild(caption);

      var trh = page.createElement("tr");
      trh.appendChild(createTdTh(page, TH, "Attribute Name"));
      trh.appendChild(createTdTh(page, TH, "In Apache Configuration", "text-center"));
      trh.appendChild(createTdTh(page, TH, "In attributes-map.xml", "text-center"));

      var thead = page.createElement("thead");
      thead.appendChild(trh);
      table.appendChild(thead);

      var tbody = page.createElement("tbody");
      for (String attrname : allAttributeNames) {
        var tr = page.createElement("tr");
        tr.appendChild(createTdTh(page, TD, attrname));
        tr.appendChild(
            createTdTh(page, TD, jkEnvVars.contains(attrname) ? YES : NO, "text-center"));
        tr.appendChild(
            createTdTh(page, TD, userAttributeNames.contains(attrname) ? YES : NO, "text-center"));
        tbody.appendChild(tr);
      }
      table.appendChild(tbody);

      var p0 = page.createElement("p");
      p0.setTextContent("The following attributes are exposed to this application:");
      p0.appendChild(table);

      var p1 = page.createElement("p");
      p1.appendChild(createLogoutLink(page, logoutUrl, "Logout"));

      var h1 = page.createElement("h1");
      h1.setTextContent(CONFIG_PAGE_TITLE);

      var containerDiv0 = createContainerDiv(page);

      containerDiv0.appendChild(h1);
      containerDiv0.appendChild(p0);
      containerDiv0.appendChild(p1);

      var body = page.createElement("body");
      body.appendChild(containerDiv0);

      var html = page.createElement("html");
      html.appendChild(createHtmlHead(page, CONFIG_PAGE_TITLE));
      html.appendChild(body);
      page.appendChild(html);

      return renderToByteArray(page);
    } catch (ParserConfigurationException e) {
      throw new AssertionError("Problem with rendering config page", e);
    }
  }

  private static Element createHtmlHead(Document page, String pageTitle) {
    var head = page.createElement("head");
    head.appendChild(createMetaCharset(page));
    head.appendChild(createMetaHttpEquiv0(page));
    head.appendChild(createMetaViewport(page));
    head.appendChild(createTitle(page, pageTitle));
    head.appendChild(createBootstrapLink(page));
    return head;
  }

  private static Element createTitle(Document page, String pageTitle) {
    var title = page.createElement("title");
    title.setTextContent(pageTitle);
    return title;
  }

  private static Element createContainerDiv(Document page) {
    var containerDiv = page.createElement("div");
    containerDiv.setAttribute("class", "container");
    return containerDiv;
  }

  private static Element createBootstrapLink(Document page) {
    var link = page.createElement("link");
    link.setAttribute("href",
        "https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css");
    link.setAttribute("rel", "stylesheet");
    link.setAttribute("integrity",
        "sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO");
    link.setAttribute("crossorigin", "anonymous");
    return link;
  }

  private static Element createMetaViewport(Document page) {
    var viewPort = page.createElement("meta");
    viewPort.setAttribute("content", "width=device-width, initial-scale=1, shrink-to-fit=no");
    viewPort.setAttribute("name", "viewport");
    return viewPort;
  }

  private static Element createMetaHttpEquiv0(Document page) {
    var httpEquiv0 = page.createElement("meta");
    httpEquiv0.setAttribute("content", "IE=edge");
    httpEquiv0.setAttribute("http-equiv", "X-UA-Compatible");
    return httpEquiv0;
  }

  private static Element createMetaCharset(Document page) {
    var charset = page.createElement("meta");
    charset.setAttribute("charset", "UTF-8");
    return charset;
  }

  private static Document createDocument() throws ParserConfigurationException {
    var factory = DocumentBuilderFactory.newDefaultInstance();
    var builder = factory.newDocumentBuilder();
    return builder.newDocument();
  }

  private static Element createTable(Document page, String... classes) {
    var table = page.createElement("table");
    setClasses(table, classes);
    return table;
  }

  private static Element createTdTh(Document page, String tdOrTh, String content,
      String... classes) {
    var tdOrThElement = page.createElement(tdOrTh);
    if (content != null && !content.isEmpty()) {
      tdOrThElement.setTextContent(content);
    }
    setClasses(tdOrThElement, classes);
    return tdOrThElement;
  }

  private static Element createLogoutLink(Document page, String logoutUrl, String content) {
    var a = page.createElement("a");
    a.setAttribute("href", logoutUrl);
    a.setTextContent(content);
    return a;
  }

  private static void setClasses(Element e, String... classes) {
    if (classes != null && classes.length > 0) {
      var sj = new StringJoiner(" ");
      for (String cls : classes) {
        sj.add(cls);
      }
      e.setAttribute("class", sj.toString());
    }
  }

  private static byte[] renderToByteArray(Document page) throws IOException {
    var bos = new ByteArrayOutputStream();
    var hr = new HtmlRenderer();
    hr.render(page, bos);
    return bos.toByteArray();
  }
}
