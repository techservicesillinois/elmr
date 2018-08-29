package edu.illinois.techservices.elmr;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Reads a Shibboleth {@code attribute-map.xml} document.
 * 
 * <p>
 * By default, the application will read the file {@code /etc/shibboleth/attribute-map.xml}. But if
 * you set the system property {@code edu.illinois.techservices.elmr.AttributesMapReader.file} to a
 * fully-qualified path, it will read it from there.
 */
class AttributesMapReader implements AttributesReader {

  private static final Logger LOGGER = Logger.getLogger(AttributesMapReader.class.getName());

  /**
   * The default location of the {@code attribute-map.xml} file.
   */
  static final String DEFAULT_FILE_LOCATION = "/etc/shibboleth/attribute-map.xml";

  /**
   * Name of the system property for the file to override the default value of
   * {@value #DEFAULT_FILE_LOCATION}.
   */
  static final String FILE_SYSPROP_NAME = AttributesMapReader.class.getName() + ".file";

  /**
   * {@inheritDoc}
   * 
   * <p>
   * For this implementation, the source is an XML document on the file system.
   * 
   * @param source path to an XML document on the file system.
   */
  @Override
  public List<String> getAttributeNamesFrom(String source) throws IOException {

    LOGGER.finest(() -> "Loading file " + source);
    List<String> attributeNames = List.of();
    try (var xmlIs = new FileInputStream(source)) {
      var documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
      var documentBuilder = documentBuilderFactory.newDocumentBuilder();
      var document = documentBuilder.parse(xmlIs);

      var kids = document.getChildNodes();
      for (int i = 0; i < kids.getLength(); i++) {
        var kid = kids.item(i);
        if (kid.getNodeType() == Node.ELEMENT_NODE) {
          Element e = (Element) kid;
          if (e.getTagName().equalsIgnoreCase("Attributes")) {
            attributeNames = processAttributeIds(e);
          }
        }
      }
    } catch (SAXException | ParserConfigurationException e) {
      LOGGER.warning("An error occurred reading the attribute map file: " + e.getMessage());
      LOGGER.log(Level.FINE, "", e);
    }
    return attributeNames;
  }

  /**
   * Processes the child elements of the {@code &lt;Attributes&gt;} element returning a List of the
   * values of the {@code id} attribute.
   * 
   * @param attributes the {@code &lt;Attributes&gt;} document element.
   * @return the values of the {@code id} attribute of each {@code &lt;Attribute&gt;} element.
   */
  private List<String> processAttributeIds(Element attributes) {
    List<String> ids = new ArrayList<>();
    var kids = attributes.getChildNodes();
    for (int i = 0; i < kids.getLength(); i++) {
      var kid = kids.item(i);
      if (kid.getNodeType() == Node.ELEMENT_NODE) {
        Element e = (Element) kid;
        if (e.getTagName().equalsIgnoreCase("Attribute")) {
          String idValue = e.getAttribute("id");
          if (Strings.notNullAndNotEmpty(idValue)) {
            ids.add(idValue);
          }
        }
      }
    }
    LOGGER.finest(() -> "Processed " + ids.size() + " attributes");
    return ids;
  }
}
