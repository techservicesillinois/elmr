package edu.illinois.techservices.elmr;

import static edu.illinois.techservices.elmr.ApplicationConstants.EMPTY_BYTE_ARRAY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HttpTests {

  @Test
  @DisplayName("Raw query strings are decoded and in a Map")
  void testRawQueryToMap() {
    var rawQuery = "name=foo%3Abar&name=fizz&name=buzz&name_other=value";
    Map<String, List<String>> params = Http.queryToMap(rawQuery);
    assertEquals(2, params.size());
    assertEquals(3, params.get("name").size());
    assertEquals(1, params.get("name_other").size());
    assertTrue(params.get("name").contains("foo:bar"));
  }

  @Test
  @DisplayName("Raw query strings are decoded, in a Map, and values are processed as expected")
  void testRawQueryToMapWithParameterProcessor() {
    Function<String, List<String>> commaDelimitedStringToList = s -> Arrays.asList(s.split(","));
    Map<String, Function<String, List<String>>> parameterProcessors = Map.of("multi", commaDelimitedStringToList);
    var rawQuery = "name=foo%3Abar&name=fizz&name=buzz&name_other=value&multi=one,2,three";
    Map<String, List<String>> params = Http.queryToMap(rawQuery, parameterProcessors);
    assertEquals(3, params.size());
    assertEquals(3, params.get("multi").size());
    assertEquals(3, params.get("name").size());
    assertEquals(1, params.get("name_other").size());
    assertTrue(params.get("name").contains("foo:bar"));
  }

  @Test
  @DisplayName("Content-Type is added to a List of values")
  void testAddingContentTypeHeaders() {
    Map<String, List<String>> responseHeaders = new HashMap<>();
    Http.addContentTypeResponseHeaders(responseHeaders, ContentType.HTML);
    assertEquals(1, responseHeaders.get("Content-Type").size());
    Http.addContentTypeResponseHeaders(responseHeaders, ContentType.TEXT);
    assertEquals(2, responseHeaders.get("Content-Type").size());
  }

  @Test
  @DisplayName("Ensure response with No Content status has a location header set in the response")
  void testSendingLocationResponse() {
    MockHttpExchange exchange = new MockHttpExchange("POST");
    var location = "http://localhost:8087/something";
    try {
      Http.sendResponseWithLocationNoContent(exchange, HttpStatus.NO_CONTENT, ContentType.HTML, location);
      assertTrue(exchange.isClosed());
      assertEquals(HttpStatus.NO_CONTENT.getStatusCode(), exchange.getStatusCode());
      var outputStream = (ByteArrayOutputStream) exchange.getResponseOS();
      assertEquals(0, outputStream.toByteArray().length);
      assertEquals(-1, exchange.getResponseLength());
      assertIterableEquals(List.of(location), exchange.getResponseHeaders().get("Location"));
      assertIterableEquals(List.of(ContentType.HTML.mime()), exchange.getResponseHeaders().get("Content-Type"));
    } catch (IOException e) {
      fail(e);
    }
  }

  @Test
  @DisplayName("HEAD response should have no body but all the other headers you'd expect with GET")
  void testHeadResponse() {
    MockHttpExchange exchange = new MockHttpExchange("HEAD");
    try {
      Http.sendResponse(exchange, HttpStatus.OK, EMPTY_BYTE_ARRAY, ContentType.HTML);
      assertTrue(exchange.isClosed());
      assertEquals(HttpStatus.OK.getStatusCode(), exchange.getStatusCode());
      var outputStream = (ByteArrayOutputStream) exchange.getResponseOS();
      assertEquals(0, outputStream.toByteArray().length);
      assertEquals(-1, exchange.getResponseLength());
      assertIterableEquals(List.of(ContentType.HTML.mime()), exchange.getResponseHeaders().get("Content-Type"));
    } catch (IOException e) {
      fail(e);
    }
  }

  @Test
  @DisplayName("GET response should have a body, Content-Type, and status")
  void testRegularGetRequest() {
    MockHttpExchange exchange = new MockHttpExchange("GET");
    byte[] content = "<!DOCTYPE html><html><head><title>HttpTests - testRegularGetRequest()</title></head><body><h1>HttpTests - testRegularGetRequest()</h1><p>Unit testing here.</body></html>"
        .getBytes();
    try {
      Http.sendResponse(exchange, HttpStatus.OK, content, ContentType.HTML);
      assertTrue(exchange.isClosed());
      assertEquals(HttpStatus.OK.getStatusCode(), exchange.getStatusCode());
      var outputStream = (ByteArrayOutputStream) exchange.getResponseOS();
      assertArrayEquals(content, outputStream.toByteArray());
      assertEquals(content.length, exchange.getResponseLength());
      assertIterableEquals(List.of(ContentType.HTML.mime()), exchange.getResponseHeaders().get("Content-Type"));
    } catch (IOException e) {
      fail(e);
    }
  }

  @Test
  @DisplayName("Last name component of a path is returned as expected")
  void testGetLastPathComponent() {
    String path = "/some/path/here";
    assertEquals("here", Http.getLastPathComponent(path));
  }
}
