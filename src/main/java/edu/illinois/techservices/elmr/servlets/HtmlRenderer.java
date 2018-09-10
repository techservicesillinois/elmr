package edu.illinois.techservices.elmr.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Set;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Renders a {@link Document} in Html5.
 *
 * <p>
 * Use the following System properties to control formatting of output:
 *
 * <dl>
 * <dt>{@code edu.illinois.techservices.elmr.servlets.HtmlRenderer.formattedHtml}
 * <dd>Set to {@code true} on the command line to format the output. Output is indented. Default is
 * {@code false};
 * <dt>{@code edu.illinois.techservices.elmr.servlets.HtmlRenderer.indentSpaces}
 * <dd>Set the number of spaces to indent the output. This is applied only if
 * {@code edu.illinois.techservices.elmr.servlets.HtmlRenderer.formattedHtml} is {@code true}.
 * Default value is {@value #DEFAULT_INDENT_SPACES}.
 * </dl>
 */
class HtmlRenderer {

  /**
   * Html5 tags that have both an open and close tag.
   */
  private static final Set<String> CLOSED_TAGS = Set.of("html", "head", "body", "script", "div",
      "title", "section", "h1", "table", "th", "tr", "td", "caption");

  /**
   * Html5 tags that are inline, meaning the contents will appear on the same line as the tags.
   */
  private static final Set<String> INLINE_TAGS = Set.of("title", "h1", "th", "td", "caption");

  /**
   * Html5 tags that are block tags, but open.
   *
   * @see #renderText(Text, Writer).
   */
  private static final Set<String> OPEN_PARENT_BLOCK_TAGS = Set.of("p");

  /**
   * Indicates output should be formatted.
   */
  private static final boolean FORMATTED_HTML =
      Boolean.getBoolean(HtmlRenderer.class.getName() + ".formattedHtml");

  private static final int DEFAULT_INDENT_SPACES = 2;

  /**
   * Number of spaces to indent children when formatting output. Default value is
   * {@value #DEFAULT_INDENT_SPACES}.
   */
  private static final int INDENT_SPACES =
      Integer.getInteger(HtmlRenderer.class.getName() + ".indentSpaces", DEFAULT_INDENT_SPACES);

  /**
   * Multiplier for number of spaces to indent children when formatting output.
   */
  private int indentLevel = 0;

  /**
   * Renders the given Document to the given {@link OutputStream}.
   *
   * @param doc Document that will be rendered.
   * @param os  OutputStream the document will be written to.
   * @throws IOException if an IOException occurs during writing.
   */
  void render(Document doc, OutputStream os) throws IOException {
    try (var osw = new OutputStreamWriter(os)) {
      render(doc, osw);
    }
  }

  /**
   * Renders the given Document to the given {@link Writer}.
   *
   * @param doc Document that will be rendered.
   * @param w   Writer the document will be written to.
   * @throws IOException if an IOException occurs during writing.
   */
  void render(Document doc, Writer w) throws IOException {
    w.write("<!DOCTYPE html>");
    if (FORMATTED_HTML) {
      w.write(System.getProperty("line.separator"));
    }
    NodeList kids = doc.getChildNodes();
    for (int i = 0; i < kids.getLength(); i++) {
      Node kid = kids.item(i);
      if (kid.getNodeType() == Node.ELEMENT_NODE) {
        renderElement((Element) kid, w);
      }
    }
  }

  private void renderElement(Element e, Writer w) throws IOException {
    if (FORMATTED_HTML && indentLevel > 0) {
      indent(w);
    }
    w.write('<');
    w.write(e.getTagName());
    if (e.hasAttributes()) {
      NamedNodeMap attrs = e.getAttributes();
      for (int i = 0; i < attrs.getLength(); i++) {
        w.write(' ');
        renderAttribute((Attr) attrs.item(i), w);
      }
    }
    w.write('>');
    if (FORMATTED_HTML && !INLINE_TAGS.contains(e.getTagName())) {
      w.write(System.getProperty("line.separator"));
    }
    NodeList kids = e.getChildNodes();
    for (int i = 0; i < kids.getLength(); i++) {
      Node kid = kids.item(i);
      if (kid.getNodeType() == Node.ELEMENT_NODE) {
        indentLevel++;
        renderElement((Element) kid, w);
      } else if (kid.getNodeType() == Node.TEXT_NODE) {
        renderText((Text) kid, w);
      }
    }

    if (CLOSED_TAGS.contains(e.getTagName())) {
      if (FORMATTED_HTML && indentLevel > 0 && !INLINE_TAGS.contains(e.getTagName())) {
        indent(w);
      }
      w.write("</");
      w.write(e.getTagName());
      w.write(">");
      if (FORMATTED_HTML) {
        w.write(System.getProperty("line.separator"));
      }
    }
    if (indentLevel > 0) {
      indentLevel--;
    }
  }

  /**
   * Writes spaces to the Writer.
   *
   * <p>
   * <strong>Implementation Note:</strong> The number of spaces is calculated by multiplying the
   * {@link #indentLevel} by the {@link #INDENT_SPACES}.
   *
   * @param w Writer for writing indent spaces to.
   * @throws IOException if an IOException occurs during writing.
   */
  private void indent(Writer w) throws IOException {
    char[] spaces = new char[indentLevel * INDENT_SPACES];
    Arrays.fill(spaces, ' ');
    w.write(spaces);
  }

  private void renderAttribute(Attr a, Writer w) throws IOException {
    w.write(a.getName());
    w.write("=\"");
    w.write(a.getValue());
    w.write("\"");
  }

  private void renderText(Text t, Writer w) throws IOException {
    if (FORMATTED_HTML && OPEN_PARENT_BLOCK_TAGS.contains(t.getParentNode().getNodeName())) {
      indent(w);
    }
    w.write(t.getWholeText());
    if (FORMATTED_HTML && OPEN_PARENT_BLOCK_TAGS.contains(t.getParentNode().getNodeName())) {
      w.write(System.getProperty("line.separator"));
    }
  }
}
