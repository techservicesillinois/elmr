package edu.illinois.techservices.elmr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Logger;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class Main {

  private static final int DEFAULT_HTTP_SERVER_PORT = 8088;

  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  private final HttpServer httpServer;

  public static void main(String... args) {
    var port = DEFAULT_HTTP_SERVER_PORT;
    var argIdx = 0;
    while (argIdx < args.length) {
      var arg = args[argIdx];
      switch (arg) {
      case "-h":
      case "--help":
        showUsageAndExit(2);
        break;
      default:
        if (arg.startsWith("-")) {
          System.err.printf("Unknown option %s%n", arg);
          showUsageAndExit(1);
        } else {
          try {
            port = Integer.valueOf(arg);
          } catch (NumberFormatException e) {
            LOGGER.config(() -> String.format("%s is not a valid port number, defaulting to %d%n", arg,
                DEFAULT_HTTP_SERVER_PORT));
            port = DEFAULT_HTTP_SERVER_PORT;
          }
        }
        break;
      }
      argIdx++;
    }

    try {
      final var main = new Main(port);

      var staticResourceHandler = new StaticResourceHandler();

      List<Filter> readFilters = List.of();
      main.addHandler("/js", staticResourceHandler, readFilters);
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          main.shutdown();
        }
      });
      main.serveHttp();
    } catch (Exception e) {
      System.err.printf("%s%n", e.getMessage());
      System.exit(1);
    }
  }

  private static void showUsageAndExit(int status) {
    showUsage();
    System.exit(status);
  }

  private static void showUsage() {
    System.err.printf("Usage: %s [port]|[Options]%n", Main.class.getName());
    System.err.println();
    System.err.println("Integration Server for Authentication.");
    System.err.println();
    System.err.println("Arguments:");
    System.err.println();
    System.err.println(
        "  port                   port the server will listen on (default is " + DEFAULT_HTTP_SERVER_PORT + ")");
    System.err.println();
    System.err.println("Options:");
    System.err.println();
    System.err.println("  -h, --help       Show this help and exit");
  }

  Main(int port) throws IOException {
    httpServer = HttpServer.create(new InetSocketAddress(port), 0);
  }

  void addHandler(String path, HttpHandler handler, List<Filter> filters) {
    var context = httpServer.createContext(path);
    context.getFilters().addAll(filters);
    context.setHandler(handler);
  }

  void serveHttp() {
    LOGGER.info(() -> String.format("Running with PID %d", ProcessHandle.current().pid()));
    httpServer.start();
  }

  void shutdown() {
    LOGGER.warning("Stopping HTTP server...");
    httpServer.stop(0);
  }
}
