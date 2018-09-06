package edu.illinois.techservices.elmr;

/**
 * Values used for finding the {@code attribute-map.xml} file.
 */
public class AttributeMapReader {

  /**
   * Name of the system property and context parameter for the location of a custom
   * {@code attribute-map.xml} file.
   */
  public static final String FILE_SYSPROP = AttributeMapReader.class.getName() + ".file";

  /**
   * The default location of the {@code attribute-map.xml} file.
   */
  public static final String DEFAULT_FILE_LOCATION = "/etc/shibboleth/attribute-map.xml";
}
