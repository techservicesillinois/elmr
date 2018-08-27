package edu.illinois.techservices.elmr;

import static edu.illinois.techservices.elmr.ApplicationConstants.EMPTY_BYTE_ARRAY;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.sun.net.httpserver.HttpExchange;

/**
 * Utilities for handling Http responses and performing Http text
 * transformations.
 */
final class Http {

  private Http() {
    // Empty constructor prevents instantiation.
  }

  /**
   * Sends an Http response with a {@code Location} header and no content.
   *
   * @param exchange    the HttpExchange
   * @param status      the status for the response
   * @param contentType the content type for the response
   * @param location    value of the {@code Location} header
   *
   * @throws IOException if an IOException occurs
   */
  static void sendResponseWithLocationNoContent(HttpExchange exchange, HttpStatus status, ContentType contentType,
      String location) throws IOException {
    var responseHeaders = new HashMap<String, List<String>>();
    addContentTypeResponseHeaders(responseHeaders, contentType);
    responseHeaders.put("Location", List.of(location));
    sendResponse(exchange, status, EMPTY_BYTE_ARRAY, responseHeaders);
  }

  /**
   * Sends an Http response.
   *
   * <p>
   * This method lets you specify a {@code Content-Type} and status.
   *
   * @param exchange    the HttpExchange
   * @param status      the status for the response
   * @param content     the response body
   * @param contentType the content type for the response
   *
   * @throws IOException if an IOException occurs
   */
  static void sendResponse(HttpExchange exchange, HttpStatus status, byte[] content, ContentType contentType)
      throws IOException {
    var responseHeaders = new HashMap<String, List<String>>();
    addContentTypeResponseHeaders(responseHeaders, contentType);
    sendResponse(exchange, status, content, responseHeaders);
  }

  /**
   * Sends an Http response.
   *
   * <p>
   * This method lets you specify any response headers.
   *
   * @param exchange        the HttpExchange
   * @param status          the status for the response
   * @param content         the response body
   * @param responseHeaders the names and values of the response headers.
   *
   * @throws IOException if an IOException occurs
   */
  static void sendResponse(HttpExchange exchange, HttpStatus status, byte[] content,
      Map<String, List<String>> responseHeaders) throws IOException {
    var h = exchange.getResponseHeaders();
    if (responseHeaders != null) {
      for (String headerName : responseHeaders.keySet()) {
        var values = responseHeaders.get(headerName);
        for (String value : values) {
          h.add(headerName, value);
        }
      }
    }

    // Avoid NPE when writing response by setting content length to -1 when
    // request method is HEAD or byte array is 0-length.
    var length = exchange.getRequestMethod().equals("HEAD") || content.length == 0 || status == HttpStatus.NO_CONTENT
        ? -1
        : content.length;
    exchange.sendResponseHeaders(status.getStatusCode(), length);

    if (content.length > 0) {
      try (var out = exchange.getResponseBody()) {
        out.write(content);
        out.flush();
      }
    }

    exchange.close();
  }

  /**
   * Adds the given ContentType to the Map of response headers, appending the
   * value if the ContentType is already set.
   *
   * @param contentType     the content type for the response
   * @param responseHeaders the names and values of the response headers.
   */
  static void addContentTypeResponseHeaders(Map<String, List<String>> responseHeaders, ContentType contentType) {
    if (responseHeaders.containsKey("Content-Type")) {
      var values = new ArrayList<String>();
      values.addAll(responseHeaders.get("Content-Type"));
      values.add(contentType.mime());
      responseHeaders.replace("Content-Type", values);
    } else {
      responseHeaders.put("Content-Type", List.of(contentType.mime()));
    }
  }

  /**
   * Simple utility that adds CORS headers to the response headers when the
   * response content type is json and an ajax call is made.
   *
   * @param responseHeaders the names and values of the response headers.
   * @see <a href="https://www.w3.org/TR/cors/">CORS Specification</a>
   */
  static void addCorsToResponseHeaders(Map<String, List<String>> responseHeaders) {
    responseHeaders.put("Access-Control-Allow-Origin", List.of("*"));
    responseHeaders.put("Access-Control-Allow-Headers", List.of("origin", "content-type", "accept"));
  }

  /**
   * Converts the query String into a Map whose keys are the query parameter names
   * and whose values are Lists of their Url-decoded values.
   *
   * @param rawQuery query string from the Url, possibly url-encoded.
   */
  static Map<String, List<String>> queryToMap(String rawQuery) {
    return queryToMap(rawQuery, new HashMap<>());
  }

  /**
   * Converts the query String into a Map whose keys are the query parameter names
   * and whose values are Lists of their Url-decoded values.
   *
   * @param rawQuery            query string from the Url, possibly url-encoded.
   * @param parameterProcessors Map whose keys are parameter names and whose
   *                            values are {@link Function}s that process the
   *                            parameter values.
   */
  static Map<String, List<String>> queryToMap(String rawQuery,
      Map<String, Function<String, List<String>>> parameterProcessors) {
    var decodedParameters = new HashMap<String, List<String>>();
    if (Strings.notNullAndNotEmpty(rawQuery)) {
      var parameters = rawQuery.split("&");
      for (String parameter : parameters) {
        var param = parameter.split("=");
        try {
          var value = decodedParameters.get(param[0]);
          if (value == null) {
            value = new ArrayList<>();
          }
          if (param.length > 1 && Strings.notNullAndNotEmpty(param[1])) {
            if (parameterProcessors.containsKey(param[0])) {
              value.addAll(parameterProcessors.get(param[0]).apply(param[1]));
            } else {
              value.add(URLDecoder.decode(param[1], "UTF-8"));
            }
            decodedParameters.put(param[0], value);
          }
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return decodedParameters;
  }

  /**
   * Returns the last component of a path delimited by {@code /}.
   *
   * @param uriPath the path of a Uri.
   */
  static String getLastPathComponent(String uriPath) {
    var pathComponents = uriPath.split("/");
    return pathComponents[pathComponents.length - 1];
  }
}
