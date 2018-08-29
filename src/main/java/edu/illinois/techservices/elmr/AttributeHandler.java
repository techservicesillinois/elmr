package edu.illinois.techservices.elmr;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class AttributeHandler implements HttpHandler {

  private static Logger LOGGER = Logger.getLogger(AttributeHandler.class.getName());

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    var fileLocation = System.getProperty(AttributesMapReader.FILE_SYSPROP_NAME);
    if (fileLocation == null || fileLocation.isEmpty()) {
      fileLocation = AttributesMapReader.DEFAULT_FILE_LOCATION;
    }
    var ar = new AttributesMapReader();
    var ids = ar.getAttributeNamesFrom(fileLocation);

    Map<String, List<String>> env = new HashMap<>();
    for (String header : exchange.getRequestHeaders().keySet()) {
      var newKey = Strings.normalize(
          header.replace(Strings.normalize(ApplicationConstants.AUTHN_HEADER_PREFIX), ""));
      env.put(newKey, exchange.getRequestHeaders().get(header));
    }
    LOGGER.finer(() -> env.toString());
    Map<String, Object> output = ids.stream().filter(id -> env.containsKey(Strings.normalize(id)))
        .collect(Collectors.toMap(Function.identity(), id -> env.get(Strings.normalize(id))));
    Http.sendResponse(exchange, HttpStatus.OK, Json.renderObject(output).getBytes(),
        ContentType.JSON);
  }

}
