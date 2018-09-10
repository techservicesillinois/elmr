package edu.illinois.techservices.elmr.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ApacheConfigTest {

  private static ByteArrayInputStream inputBytes;

  @BeforeAll
  static void setupTestClass() {
    StringBuilder apacheConf = new StringBuilder();
    apacheConf.append("#").append(System.getProperty("line.separator"));
    apacheConf.append("# mod_jk.conf").append(System.getProperty("line.separator"));
    apacheConf.append("#").append(System.getProperty("line.separator"));
    apacheConf.append("# Shows how to configure mod_jk to pass Shibboleth attributes from ")
        .append(System.getProperty("line.separator"));
    apacheConf.append("# mod_shib to Tomcat.").append(System.getProperty("line.separator"));
    apacheConf.append("#").append(System.getProperty("line.separator"));
    apacheConf.append("# Tomcat's conf/server.xml file must define an AJP connector.")
        .append(System.getProperty("line.separator"));
    apacheConf.append("#").append(System.getProperty("line.separator"));
    apacheConf.append(System.getProperty("line.separator"));
    apacheConf.append("# Set the paths for routing requests to Tomcat.")
        .append(System.getProperty("line.separator"));
    apacheConf.append("#").append(System.getProperty("line.separator"));
    apacheConf.append("JkMount /elmr elmr").append(System.getProperty("line.separator"));
    apacheConf.append("JkMount /elmr/* elmr").append(System.getProperty("line.separator"));
    apacheConf.append(System.getProperty("line.separator"));
    apacheConf.append("# Expose Shibboleth attribute names as environment variables. These ")
        .append(System.getProperty("line.separator"));
    apacheConf.append("# have to match the attributes in your attributes-map.xml file.")
        .append(System.getProperty("line.separator"));
    apacheConf.append("#").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar displayName").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar eduPersonAffiliation").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar eduPersonOrgDn").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar eduPersonPrimaryAffiliation")
        .append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar eduPersonPrincipalName")
        .append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar eduPersonScopedAffiliation")
        .append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar eduPersonTargetedID").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar givenName").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar iTrustAffiliation").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar iTrustSuppress").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar mail").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar sn").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar commonName").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar eduPersonNickname").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar isMemberOf").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar iTrustUIN").append(System.getProperty("line.separator"));
    apacheConf.append("JkEnvVar uid").append(System.getProperty("line.separator"));
    apacheConf.append(System.getProperty("line.separator"));
    apacheConf.append("# Location directives to turn on Shibboleth authentication.")
        .append(System.getProperty("line.separator"));
    apacheConf.append("#").append(System.getProperty("line.separator"));
    apacheConf.append("<Location /Shibboleth.sso>").append(System.getProperty("line.separator"));
    apacheConf.append("SetHandler shib").append(System.getProperty("line.separator"));
    apacheConf.append("</Location>").append(System.getProperty("line.separator"));
    apacheConf.append(System.getProperty("line.separator"));
    apacheConf.append("<Location /elmr/session>").append(System.getProperty("line.separator"));
    apacheConf.append("Allow from all").append(System.getProperty("line.separator"));
    apacheConf.append("AuthType shibboleth").append(System.getProperty("line.separator"));
    apacheConf.append("ShibRequestSetting requireSession 1")
        .append(System.getProperty("line.separator"));
    apacheConf.append("ShibRequestSetting ShibUseEnvironment 1")
        .append(System.getProperty("line.separator"));
    apacheConf.append("Require valid-user").append(System.getProperty("line.separator"));
    apacheConf.append("</Location>").append(System.getProperty("line.separator"));
    apacheConf.append(System.getProperty("line.separator"));
    apacheConf.append("<Location /elmr/config>").append(System.getProperty("line.separator"));
    apacheConf.append("Allow from all").append(System.getProperty("line.separator"));
    apacheConf.append("AuthType shibboleth").append(System.getProperty("line.separator"));
    apacheConf.append("ShibRequestSetting requireSession 1")
        .append(System.getProperty("line.separator"));
    apacheConf.append("ShibRequestSetting ShibUseEnvironment 1")
        .append(System.getProperty("line.separator"));
    apacheConf.append("Require valid-user").append(System.getProperty("line.separator"));
    apacheConf.append("</Location>").append(System.getProperty("line.separator"));

    inputBytes = new ByteArrayInputStream(apacheConf.toString().getBytes());
  }

  @Test
  void testGetJkEnvVars() {
    try {
      ApacheConfig conf = new ApacheConfig(inputBytes);
      List<String> jkEnvVars = conf.getJkEnvVars();
      Logger.getLogger(ApacheConfigTest.class.getName()).fine(jkEnvVars.toString());
      assertEquals(17, jkEnvVars.size());
    } catch (Exception e) {
      fail(e);
    }
  }
}
