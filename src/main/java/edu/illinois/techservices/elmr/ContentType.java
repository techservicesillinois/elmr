package edu.illinois.techservices.elmr;

import java.util.Map;

/**
 * Content Type values.
 */
enum ContentType {

  /**
   * Generic binary type.
   */
  BINARY("application/octet"),

  HTML("text/html"),

  JAVASCRIPT("application/javascript"),

  JSON("application/json"),

  /**
   * Generic text type.
   */
  TEXT("text/plain"),

  XML("text/xml");

  private final String mimeType;

  private static final Map<String, ContentType> BY_MIME = Map.of("application/octet", BINARY, "text/html", HTML,
      "application/javascript", JAVASCRIPT, "applicaiton/json", JSON, "text/plain", TEXT, "text/xml", XML);

  private static final Map<String, ContentType> BY_EXT = Map.of("html", HTML, "js", JAVASCRIPT, "json", JSON, "txt",
      TEXT, "xml", XML);

  private ContentType(String mimeType) {
    this.mimeType = mimeType;
  }

  String mime() {
    return mimeType;
  }

  /**
   * Returns the ContentType associated with the given MIME type.
   * 
   * <p>
   * If the type isn't recognized, then if the MIME type starts with
   * {@code text/}, {@link #TEXT} is returned. Otherwise {@link #BINARY} is
   * returned.
   * 
   * @param mime the MIME type.
   */
  static ContentType getFromMime(String mime) {
    if (BY_MIME.containsKey(mime)) {
      return BY_MIME.get(mime);
    } else if (mime.startsWith("text/")) {
      return TEXT;
    } else {
      return BINARY;
    }
  }

  /**
   * Returns the ContentType associated with the given file extension.
   * 
   * <p>
   * If the file extension isn't recognized, then {@link #BINARY} is returned.
   * 
   * @param ext the file extension.
   */
  static ContentType getFromExtension(String ext) {
    if (BY_EXT.containsKey(ext)) {
      return BY_EXT.get(ext);
    } else {
      return BINARY;
    }
  }
}
