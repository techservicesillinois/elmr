package edu.illinois.techservices.elmr.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import edu.illinois.techservices.elmr.AttributeMapHandler;
import edu.illinois.techservices.elmr.AttributeMapReader;

/**
 * Loads a List of attribute names at startup and caches it in a context parameter.
 * 
 * <p>
 * The name for an {@code attribute-map.xml} file can be set 3 ways and are searched in this order:
 * <ol>
 * <li>The value of the system property
 * {@code edu.illinois.techservices.elmr.AttributeMapReader.file}.
 * <li>The value of the context parameter
 * {@code edu.illinois.techservices.elmr.AttributeMapReader.file}.
 * <li>The default location of the file {@code /etc/shibboleth/attribute-map.xml} (if neither of the
 * above are set).
 * </ol>
 * 
 * <p>
 * When set, the file is loaded and parsed for the {@code id} values of the {@code Attribute}
 * elements. These are stored in a List that is saved to a context parameter named
 * {@code edu.illinois.techservices.elmr.servlets.attributes}. This List can be accessed from the
 * servlets in this application.
 */
@WebListener
public class AttributeMapContextListener implements ServletContextListener {

  private static final Logger LOGGER =
      Logger.getLogger(AttributeMapContextListener.class.getName());

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    LOGGER.config("Initializing attribute map ids...");

    var fileLocation = System.getProperty(AttributeMapReader.FILE_SYSPROP);
    if (fileLocation == null || fileLocation.isEmpty()) {
      fileLocation = sce.getServletContext().getInitParameter(AttributeMapReader.FILE_SYSPROP);
      if (fileLocation == null || fileLocation.isEmpty()) {
        LOGGER.config("Reading file from default location");
        fileLocation = AttributeMapReader.DEFAULT_FILE_LOCATION;
      } else {
        LOGGER.config("Reading file set by context parameter " + AttributeMapReader.FILE_SYSPROP);
      }
    } else {
      LOGGER.config("Reading file set by system property " + AttributeMapReader.FILE_SYSPROP);
    }

    LOGGER.config("Caching Shibboleth attributes from file " + fileLocation);
    AttributeMapHandler amh = new AttributeMapHandler();
    try (var in = new FileInputStream(new File(fileLocation))) {
      var parserFactory = SAXParserFactory.newInstance();
      var parser = parserFactory.newSAXParser();
      parser.parse(in, amh);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      LOGGER.log(Level.SEVERE, "Problem reading " + fileLocation + "! Cannot start application!",
          e);
      throw new RuntimeException(e);
    }
    sce.getServletContext().setAttribute(
        AttributeMapContextListener.class.getPackageName() + ".attributes",
        Collections.unmodifiableList(amh.getAttributeNames()));
    LOGGER.config("Attributes cached; access with context property "
        + AttributeMapContextListener.class.getPackageName() + ".attributes");
  }
}
