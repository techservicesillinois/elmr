package edu.illinois.techservices.elmr;

import java.io.IOException;
import java.util.List;

interface AttributesReader {

  /**
   * Returns a List of attribute names from the given source which can be a file, URL, or any other
   * source.
   * 
   * @param source the source of the attribute names.
   */
  List<String> getAttributeNamesFrom(String source) throws IOException;
}
