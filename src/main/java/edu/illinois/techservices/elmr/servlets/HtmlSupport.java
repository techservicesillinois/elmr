package edu.illinois.techservices.elmr.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class HtmlSupport {

  private HtmlSupport() {
    // Empty constructor prevents instantiation.
  }

  static byte[] renderAttributesPage(Set<String> allAttributeNames, List<String> jkEnvVars,
      List<String> userAttributeNames) throws IOException {
    try {
      var factory = DocumentBuilderFactory.newDefaultInstance();
      var builder = factory.newDocumentBuilder();

      var page = builder.newDocument();

      var html = page.createElement("html");

      var head = page.createElement("head");

      var charset = page.createElement("meta");
      charset.setAttribute("charset", "UTF-8");

      var httpEquiv0 = page.createElement("meta");
      httpEquiv0.setAttribute("content", "IE=edge");
      httpEquiv0.setAttribute("http-equiv", "X-UA-Compatible");

      var viewPort = page.createElement("meta");
      viewPort.setAttribute("content", "width=device-width, initial-scale=1, shrink-to-fit=no");
      viewPort.setAttribute("name", "viewport");

      var title = page.createElement("title");
      title.setTextContent("elmr - Apache Configuration");

      var link = page.createElement("link");
      link.setAttribute("href", "https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css");
      link.setAttribute("rel", "stylesheet");
      link.setAttribute("integrity", "sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO");
      link.setAttribute("crossorigin", "anonymous");

      head.appendChild(charset);
      head.appendChild(httpEquiv0);
      head.appendChild(viewPort);
      head.appendChild(title);
      head.appendChild(link);

      html.appendChild(head);

      var body = page.createElement("body");

      var containerDiv0 = page.createElement("div");
      containerDiv0.setAttribute("class", "container");

      var h1 = page.createElement("h1");
      h1.setTextContent("elmr - Apache Configuration");

      var p0 = page.createElement("p");
      p0.setTextContent("The following attributes are exposed to this application:");

      var table = page.createElement("table");
      table.setAttribute("class", "table table-striped");
      var caption = page.createElement("caption");
      caption.setTextContent("Configured Attribute Names");
      table.appendChild(caption);

      var thead = page.createElement("thead");
      var trh = page.createElement("tr");

      var th0 = page.createElement("th");
      th0.setTextContent("Attribute Name");
      
      var th1 = page.createElement("th");
      th1.setTextContent("In Apache Configuration");
      th1.setAttribute("class", "text-center");
      
      var th2 = page.createElement("th");
      th2.setTextContent("In attributes-map.xml");
      th2.setAttribute("class", "text-center");
      
      trh.appendChild(th0);
      trh.appendChild(th1);
      trh.appendChild(th2);

      thead.appendChild(trh);
      table.appendChild(thead);

      var tbody = page.createElement("tbody");

      for (String attrname : allAttributeNames) {
        var tr = page.createElement("tr");
        
        var td0 = page.createElement("td");
        td0.setTextContent(attrname);

        var td1 = page.createElement("td");
        if (jkEnvVars.contains(attrname)) {
          td1.setTextContent("Yes");
        } else {
          td1.setTextContent("No");
        }
        td1.setAttribute("class", "text-center");
        
        var td2 = page.createElement("td");
        if (userAttributeNames.contains(attrname)) {
          td2.setTextContent("Yes");
        } else {
          td2.setTextContent("No");
        }
        td2.setAttribute("class", "text-center");

        tr.appendChild(td0);
        tr.appendChild(td1);
        tr.appendChild(td2);

        tbody.appendChild(tr);
      }

      table.appendChild(tbody);
      p0.appendChild(table);

      containerDiv0.appendChild(h1);
      containerDiv0.appendChild(p0);

      body.appendChild(containerDiv0);

      html.appendChild(body);
      page.appendChild(html);

      var bos = new ByteArrayOutputStream();
      HtmlRenderer hr = new HtmlRenderer();
      hr.render(page, bos);
      return bos.toByteArray();
    } catch (ParserConfigurationException e) {
      throw new AssertionError("Problem with rendering error page", e);
    }
  }
}
