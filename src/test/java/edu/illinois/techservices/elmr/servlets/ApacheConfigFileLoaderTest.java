package edu.illinois.techservices.elmr.servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import javax.servlet.ServletContextEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ApacheConfigFileLoaderTest {

  private static final String TEMP_APACHE_CONF_FILE_NAME_PREFIX =
      ApacheConfigFileLoaderTest.class.getName().replaceAll("\\.", "-");

  private static final String TEMP_APACHE_CONF_FILE_NAME_SUFFIX = ".conf";

  private static File conf;

  private static String confFilename;

  @BeforeAll
  static void setupTest() throws Exception {
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

    conf = File.createTempFile(TEMP_APACHE_CONF_FILE_NAME_PREFIX, TEMP_APACHE_CONF_FILE_NAME_SUFFIX,
        new File(System.getProperty("java.io.tmpdir")));
    conf.deleteOnExit();
    try (var fw = new FileWriter(conf)) {
      fw.write(apacheConf.toString());
    }

    confFilename = conf.getAbsolutePath();
  }

  @Test
  void testNoParametersSet() {
    var context =
        ProxyFactories.createServletContextProxy(new ServletApiInvocationHandler.Builder().build());
    var sce = new ServletContextEvent(context);
    var apacheConfigFileLoader = new ApacheConfigFileLoader();
    apacheConfigFileLoader.contextInitialized(sce);
    var acf = (ApacheConfig) sce.getServletContext()
        .getAttribute(ServletConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME);

    // Asserts are here.
    assertTrue(acf.getJkEnvVars().isEmpty());
  }

  @Test
  void testContextParametersSet() {
    var initParameters = new HashMap<String, String>();
    initParameters.put(ServletConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME, confFilename);
    var context = ProxyFactories.createServletContextProxy(
        new ServletApiInvocationHandler.Builder().addInitParameters(initParameters).build());
    var sce = new ServletContextEvent(context);
    var apacheConfigFileLoader = new ApacheConfigFileLoader();
    apacheConfigFileLoader.contextInitialized(sce);
    var acf = (ApacheConfig) sce.getServletContext()
        .getAttribute(ServletConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME);

    // Asserts are here.
    assertFalse(acf.getJkEnvVars().isEmpty());
    assertTrue(acf.getJkEnvVars().contains("displayName"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonAffiliation"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonOrgDn"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonPrimaryAffiliation"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonPrincipalName"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonScopedAffiliation"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonTargetedID"));
    assertTrue(acf.getJkEnvVars().contains("givenName"));
    assertTrue(acf.getJkEnvVars().contains("iTrustAffiliation"));
    assertTrue(acf.getJkEnvVars().contains("iTrustSuppress"));
    assertTrue(acf.getJkEnvVars().contains("mail"));
    assertTrue(acf.getJkEnvVars().contains("sn"));
    assertTrue(acf.getJkEnvVars().contains("commonName"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonNickname"));
    assertTrue(acf.getJkEnvVars().contains("isMemberOf"));
    assertTrue(acf.getJkEnvVars().contains("iTrustUIN"));
    assertTrue(acf.getJkEnvVars().contains("uid"));
  }

  @Test
  void testSystemPropertySet() {
    var initParameters = new HashMap<String, String>();
    initParameters.put(ServletConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME, confFilename);
    var context = ProxyFactories.createServletContextProxy(
        new ServletApiInvocationHandler.Builder().addInitParameters(initParameters).build());
    var sce = new ServletContextEvent(context);
    var apacheConfigFileLoader = new ApacheConfigFileLoader();

    // Set the System Property here
    System.setProperty(ServletConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME, confFilename);

    apacheConfigFileLoader.contextInitialized(sce);
    var acf = (ApacheConfig) sce.getServletContext()
        .getAttribute(ServletConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME);

    // Now unset the property so that other tests are contaminated with this value
    System.clearProperty(ServletConstants.APACHE_CONFIG_CONTEXT_PARAM_NAME);

    // Asserts are here.
    assertFalse(acf.getJkEnvVars().isEmpty());
    assertTrue(acf.getJkEnvVars().contains("displayName"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonAffiliation"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonOrgDn"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonPrimaryAffiliation"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonPrincipalName"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonScopedAffiliation"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonTargetedID"));
    assertTrue(acf.getJkEnvVars().contains("givenName"));
    assertTrue(acf.getJkEnvVars().contains("iTrustAffiliation"));
    assertTrue(acf.getJkEnvVars().contains("iTrustSuppress"));
    assertTrue(acf.getJkEnvVars().contains("mail"));
    assertTrue(acf.getJkEnvVars().contains("sn"));
    assertTrue(acf.getJkEnvVars().contains("commonName"));
    assertTrue(acf.getJkEnvVars().contains("eduPersonNickname"));
    assertTrue(acf.getJkEnvVars().contains("isMemberOf"));
    assertTrue(acf.getJkEnvVars().contains("iTrustUIN"));
    assertTrue(acf.getJkEnvVars().contains("uid"));
  }
}
