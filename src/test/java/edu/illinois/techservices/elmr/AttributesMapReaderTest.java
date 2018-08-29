package edu.illinois.techservices.elmr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AttributesMapReaderTest {

  private static final String TEMP_XML_FILE_NAME_PREFIX =
      AttributesMapReaderTest.class.getName().replaceAll("\\.", "-");

  private static final String TEMP_XML_FILE_NAME_SUFFIX = ".xml";

  private static File xml;

  private static String xmlFilename;

  @BeforeAll
  static void setupTestClass() throws Exception {
    xml = File.createTempFile(TEMP_XML_FILE_NAME_PREFIX, TEMP_XML_FILE_NAME_SUFFIX,
        new File(System.getProperty("java.io.tmpdir")));
    xml.deleteOnExit();
    try (var fw = new FileWriter(xml)) {
      fw.write(
          "<Attributes xmlns=\"urn:mace:shibboleth:2.0:attribute-map\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
      fw.write("\n");
      fw.write("  <!-- eduPersonPrincipalName -->\n");
      fw.write(
          "  <Attribute name=\"urn:oid:1.3.6.1.4.1.5923.1.1.1.6\" id=\"eduPersonPrincipalName\">\n");
      fw.write("    <AttributeDecoder xsi:type=\"ScopedAttributeDecoder\"/>\n");
      fw.write("  </Attribute>\n");
      fw.write("\n");
      fw.write("  <!-- eduPersonTargetedID -->\n");
      fw.write(
          "  <Attribute name=\"urn:oid:1.3.6.1.4.1.5923.1.1.1.10\" id=\"eduPersonTargetedID\">\n");
      fw.write(
          "    <AttributeDecoder xsi:type=\"NameIDAttributeDecoder\" formatter=\"$NameQualifier!$SPNameQualifier!$Name\" defaultQualifiers=\"true\"/>\n");
      fw.write("  </Attribute>\n");
      fw.write("\n");
      fw.write("  <!-- eduPersonScopedAffiliation -->\n");
      fw.write("  <Attribute name=\"urn:oid:1.3.6.1.4.1.5923.1.1.1.9\" id=\"affiliation\">\n");
      fw.write(
          "    <AttributeDecoder xsi:type=\"ScopedAttributeDecoder\" caseSensitive=\"false\"/>\n");
      fw.write("  </Attribute>\n");
      fw.write("\n");
      fw.write("  <!-- eduPersonAffiliation -->\n");
      fw.write(
          "  <Attribute name=\"urn:oid:1.3.6.1.4.1.5923.1.1.1.1\" id=\"eduPersonAffiliation\">\n");
      fw.write(
          "    <AttributeDecoder xsi:type=\"StringAttributeDecoder\" caseSensitive=\"false\"/>\n");
      fw.write("  </Attribute>\n");
      fw.write("\n");
      fw.write("  <!-- eduPersonPrimaryAffiliation -->\n");
      fw.write("  <!-- eduPersonEntitlement -->\n");
      fw.write(
          "  <!-- <Attribute name=\"urn:oid:1.3.6.1.4.1.5923.1.1.1.7\" id=\"entitlement\"/> -->\n");
      fw.write("\n");
      fw.write("  <!-- eduPersonNickname -->\n");
      fw.write(
          "  <!--Attribute name=\"urn:oid:1.3.6.1.4.1.5923.1.1.1.2\" id=\"eduPersonNickname\"/-->\n");
      fw.write("\n");
      fw.write("  <!-- eduPersonOrgDN -->\n");
      fw.write("  <!-- <Attribute name=\"urn:oid:1.3.6.1.4.1.5923.1.1.1.3\" id=\"org-dn\"/> -->\n");
      fw.write("\n");
      fw.write("  <!-- Attributes defining user's name -->\n");
      fw.write("  <!-- <Attribute name=\"urn:oid:2.5.4.4\" id=\"sn\"/> -->\n");
      fw.write("  <!-- <Attribute name=\"urn:oid:2.5.4.44\" id=\"generationQualifier\"/> -->\n");
      fw.write("  <Attribute name=\"urn:oid:2.5.4.42\" id=\"givenName\"/>\n");
      fw.write(
          "  <!-- <Attribute name=\"urn:oid:1.3.6.1.4.1.11483.101.2\" id=\"iTrustMiddleName\"/> -->\n");
      fw.write("  <Attribute name=\"urn:oid:2.16.840.1.113730.3.1.241\" id=\"displayName\"/>\n");
      fw.write("\n");
      fw.write("  <!-- Other directory data about the user -->\n");
      fw.write("  <Attribute name=\"urn:oid:0.9.2342.19200300.100.1.1\" id=\"uid\"/>\n");
      fw.write("  <Attribute name=\"urn:oid:0.9.2342.19200300.100.1.3\" id=\"mail\"/>\n");
      fw.write("  <!-- <Attribute name=\"urn:oid:2.5.4.20\" id=\"telephoneNumber\"/> -->\n");
      fw.write("  <!-- <Attribute name=\"urn:oid:2.5.4.16\" id=\"postalAddress\"/> -->\n");
      fw.write("  <!-- <Attribute name=\"urn:oid:2.5.4.12\" id=\"title\"/> -->\n");
      fw.write(
          "  <Attribute name=\"urn:oid:1.3.6.1.4.1.11483.101.1\" id=\"iTrustAffiliation\"/>\n");
      fw.write(
          "  <!-- <Attribute name=\"urn:oid:1.3.6.1.4.1.11483.101.3\" id=\"iTrustSuppress\"/> -->\n");
      fw.write("  <Attribute name=\"urn:oid:1.3.6.1.4.1.11483.101.4\" id=\"iTrustUIN\"/>\n");
      fw.write("\n");
      fw.write("  <!-- isMemberOf -->\n");
      fw.write("  <!--Attribute name=\"urn:oid:1.3.6.1.4.1.5923.1.5.1.1\" id=\"member\"/-->\n");
      fw.write("\n");
      fw.write("  <!-- organizationName -->\n");
      fw.write("  <!-- <Attribute name=\"urn:oid:2.5.4.10\" id=\"o\"/> -->\n");
      fw.write("\n");
      fw.write("  <!-- organizationalUnit -->\n");
      fw.write("  <!-- <Attribute name=\"urn:oid:2.5.4.11\" id=\"ou\"/> -->\n");
      fw.write("\n");
      fw.write(
          "  <!-- <Attribute name=\"urn:oid:1.3.6.1.4.1.25178.1.2.10\" id=\"homeOrganizationType\"/> -->\n");
      fw.write("  <!-- Non-federated attributes here -->\n");
      fw.write("  <Attribute name=\"urn:oid:1.3.6.1.4.1.11483.1.30\" id=\"uiucEduSource\">\n");
      fw.write(
          "    <AttributeDecoder xsi:type=\"StringAttributeDecoder\" caseSensitive=\"false\"/>\n");
      fw.write("  </Attribute>\n");
      fw.write(
          "  <Attribute name=\"urn:oid:1.3.6.1.4.1.11483.1.42\" id=\"uiucEduStudentLevelCode\">\n");
      fw.write(
          "    <AttributeDecoder xsi:type=\"StringAttributeDecoder\" caseSensitive=\"false\"/>\n");
      fw.write("  </Attribute>\n");
      fw.write(
          "  <Attribute name=\"urn:oid:1.3.6.1.4.1.11483.1.133\" id=\"uiucEduStudentAdmitLevelCode\">\n");
      fw.write(
          "    <AttributeDecoder xsi:type=\"StringAttributeDecoder\" caseSensitive=\"false\"/>\n");
      fw.write("  </Attribute>\n");
      fw.write("</Attributes>\n");

    }
    xmlFilename = xml.getAbsolutePath();
  }

  @Test
  void testReadingAttributeIds() {
    try {
      AttributesReader ar = new AttributesMapReader();
      List<String> ids = ar.getAttributeNamesFrom(xmlFilename);
      assertTrue(ids.size() > 0);
      assertTrue(ids.contains("eduPersonPrincipalName"));
      assertTrue(ids.contains("eduPersonTargetedID"));
      assertTrue(ids.contains("affiliation"));
      assertTrue(ids.contains("eduPersonAffiliation"));
      assertFalse(ids.contains("o"));
    } catch (Exception e) {
      fail(e);
    }
  }
}
