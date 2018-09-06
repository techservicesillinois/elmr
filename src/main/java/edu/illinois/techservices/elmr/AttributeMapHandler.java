package edu.illinois.techservices.elmr;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

/**
 * SAX handler that collects the {@code id} attributes from {@code &lt;Attribute&gt;} elements in an
 * {@code attribute-map.xml} file.
 */
public class AttributeMapHandler extends DefaultHandler {

  private static final Logger LOGGER = Logger.getLogger(AttributeMapHandler.class.getName());

  private Locator locator = new LocatorImpl();

  private List<String> attributeNames = new ArrayList<>();

  @Override
  public void startDocument() {
    LOGGER.config("Starting to parse attributes map...");
  }

  @Override
  public void endDocument() {
    LOGGER
        .config("Finished parsing attributes map. Found " + attributeNames.size() + " attributes.");
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    if (qName.equals("Attribute")) {
      for (int i = 0; i < attributes.getLength(); i++) {
        if (attributes.getQName(i).equals("id")) {
          LOGGER.finer("Received <Attribute> with id " + attributes.getValue(i));
          attributeNames.add(attributes.getValue(i));
        }
      }
    }
  }

  @Override
  public void warning(SAXParseException e) {
    LOGGER.log(Level.WARNING, "A recoverable problem has occurred at line "
        + locator.getLineNumber() + ", col " + locator.getColumnNumber() + ".", e);
  }

  @Override
  public void error(SAXParseException e) {
    LOGGER.log(Level.SEVERE, "An error has occurred at line " + locator.getLineNumber() + ", col "
        + locator.getColumnNumber() + ".", e);
  }

  @Override
  public void fatalError(SAXParseException e) {
    LOGGER.log(Level.SEVERE, "A fatal error has occurred at line " + locator.getLineNumber()
        + ", col " + locator.getColumnNumber() + ".", e);
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  /**
   * Returns the List of attribute ids.
   */
  public List<String> getAttributeNames() {
    return attributeNames;
  }
}
