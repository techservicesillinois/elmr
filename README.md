# elmr

HTTP Server that reads headers from an authenticated request and persists them.

## Building

This project uses [Maven](https://maven.apache.org/) as its build system. From the project's root directory, run the command below and it will be built.

    mvn clean package

## Testing

This project uses [JUnit 5](https://junit.org/junit5/docs/current/user-guide/) as its test system. Tests will be run when the project is built, however to run tests while developing, the command below will work.

    mvn clean test

## Running

Run the jar file:

    java [system properties] -jar elmr.jar [args]

Logging is based on JDK logging and output to the console by default. You can set the logging configuration to use by specifying the `java.util.logging.config.file` system property. Press `^C` to stop the server.

Arguments & options can be supplied on the command line to run the server:

```
Usage: edu.illinois.techservices.elmr.Main [port]|[Options]

Integration Server for Authentication.

Arguments:

  port                   port the server will listen on (default is 8088)

Options:

  -h, --help       Show this help and exit
```

When you start the server, you can navigate to `http://localhost:8088/` (or to whatever port you set on startup) after starting the server.

## Troubleshooting

### Problem: Server was started with custom file location, but returns nothing

Make sure you entered the system property name and file name correctly on the command line. If either is mistyped, the XML will not be loaded and processed.

## Etymology

The name is a play on a certain brand of glue found in school children's desks. It was chosen because the purpose for this server is mostly integration sometimes referred to as "glue" code.
