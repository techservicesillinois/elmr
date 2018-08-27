package edu.illinois.techservices.elmr;

class Strings {

  private Strings() {
    // Empty constructor prevents instantiation.
  }

  static boolean notNullAndNotEmpty(String s) {
    return s != null && !s.isEmpty();
  }
}
