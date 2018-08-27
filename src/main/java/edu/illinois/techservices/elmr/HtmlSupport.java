package edu.illinois.techservices.elmr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class HtmlSupport {

  private HtmlSupport() {
    // Empty constructor prevents instantiation.
  }

  static byte[] renderErrorPage(HttpStatus status, String errorMessage) throws IOException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      Document errorPage = builder.newDocument();
      Element html = errorPage.createElement("html");

      Element head = errorPage.createElement("head");

      Element charset = errorPage.createElement("meta");
      charset.setAttribute("charset", "UTF-8");

      Element httpEquiv0 = errorPage.createElement("meta");
      httpEquiv0.setAttribute("content", "IE=edge");
      httpEquiv0.setAttribute("http-equiv", "X-UA-Compatible");

      Element viewPort = errorPage.createElement("meta");
      viewPort.setAttribute("content", "width=device-width, initial-scale=1, shrink-to-fit=no");
      viewPort.setAttribute("name", "viewport");

      Element httpEquiv1 = errorPage.createElement("meta");
      httpEquiv1.setAttribute("content", "ie=edge");
      httpEquiv1.setAttribute("http-equiv", "x-ua-compatible");

      Element title = errorPage.createElement("title");
      title.setTextContent(String.format("Error! %d - %s", status.getStatusCode(), status.getMessage()));

      head.appendChild(charset);
      head.appendChild(httpEquiv0);
      head.appendChild(viewPort);
      head.appendChild(httpEquiv1);
      head.appendChild(title);

      html.appendChild(head);

      Element body = errorPage.createElement("body");

      Element containerDiv0 = errorPage.createElement("div");
      containerDiv0.setAttribute("class", "container");

      Element section0 = errorPage.createElement("section");

      Element h1 = errorPage.createElement("h1");
      h1.setTextContent("Error!");

      Element p0 = errorPage.createElement("p");
      p0.setTextContent("An error has occurred!");

      section0.appendChild(h1);
      section0.appendChild(p0);

      containerDiv0.appendChild(section0);

      body.appendChild(containerDiv0);

      html.appendChild(body);
      errorPage.appendChild(html);

      var bos = new ByteArrayOutputStream();
      HtmlRenderer hr = new HtmlRenderer();
      hr.render(errorPage, bos);
      return bos.toByteArray();
    } catch (ParserConfigurationException e) {
      throw new AssertionError("Problem with rendering error page", e);
    }
  }
}
