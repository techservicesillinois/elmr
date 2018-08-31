package edu.illinois.techservices.elmr.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.illinois.techservices.elmr.AttributesMapReader;
import edu.illinois.techservices.elmr.ContentType;
import edu.illinois.techservices.elmr.Json;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    var fileLocation = System.getProperty(AttributesMapReader.FILE_SYSPROP_NAME);
    if (fileLocation == null || fileLocation.isEmpty()) {
      fileLocation = AttributesMapReader.DEFAULT_FILE_LOCATION;
    }
    var ar = new AttributesMapReader();
    var ids = ar.getAttributeNamesFrom(fileLocation);

    Map<String, Object> output =
        ids.stream().filter(id -> Objects.nonNull(request.getAttribute(id)))
            .collect(Collectors.toMap(Function.identity(), id -> {
              String attr = (String) request.getAttribute(id);
              if (attr.indexOf(';') > 0) {
                return Arrays.asList(attr.split(";"));
              }
              return attr;
            }));
    var json = Json.renderObject(output).getBytes();
    try (var os = response.getOutputStream()) {
      os.write(json);
      if (json.length <= 0) {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      } else {
        response.setContentType(ContentType.JSON.mime());
        response.setContentLength(json.length);
        response.setStatus(HttpServletResponse.SC_OK);
      }
    }
  }
}
