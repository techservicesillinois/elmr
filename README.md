# elmr

HTTP Server that reads Shibboleth attributes from a request and persists them for use in an application session. When the session is finished, the attributes can be erased.

elmr is packaged as a minimal [Tomcat 9](https://tomcat.apache.org/tomcat-9.0-doc/index.html) server. It can be started and stopped using the regular Tomcat scripts. See the section [Running](#running) for more details. There is no packaged support for JSPs or clustering in this installation.

## Requirements

elmr is built and run on Java-10. Please make sure it is installed and can be used.

elmr requires [Maven version 3.5.3](https://maven.apache.org/) (or newer) to be built.

elmr requires an Apache httpd web server running with `mod_jk` and `mod_shib` installed and enabled. [See below](#configuring-apache-httpd) for configuration of both.

## Building

This project uses [Maven](https://maven.apache.org/) as its build system. From the project's root directory, run the command below and it will be built.

    mvn clean package

## Testing

This project uses [JUnit 5](https://junit.org/junit5/docs/current/user-guide/) as its test system. Tests will be run when the project is built, however to run tests while developing, the command below will work.

    mvn [sysprops] clean test

System properties have to be set in order to run some tests as shown below.

Property | Description 
---|---
`edu.illinois.techservices.elmr.redis.CanConnect` | Used by `edu.illinois.techservices.elmr.SessionDataImplTests` to flag a connection to a locally running Redis instance can be connected to. Set to `true` to use the local redis instance or leave unset to not run the test.

### Test Output

When running tests, you will see the following output:

```
[INFO] Running edu.illinois.techservices.elmr.servlets.AttributeMapContextListenerTest
Sep 26, 2018 1:53:11 PM edu.illinois.techservices.elmr.servlets.AttributeMapContextListener contextInitialized
SEVERE: Problem reading /etc/shibboleth/attribute-map.xml! Cannot start application!
java.io.FileNotFoundException: /etc/shibboleth/attribute-map.xml (No such file or directory)
        at java.base/java.io.FileInputStream.open0(Native Method)
        at java.base/java.io.FileInputStream.open(FileInputStream.java:220)
        at java.base/java.io.FileInputStream.<init>(FileInputStream.java:158)
        at edu.illinois.techservices.elmr.servlets.AttributeMapContextListener.contextInitialized(AttributeMapContextListener.java:63)
        ...
```

This one is normal and is part of a test. If there are reported test failures with stack traces other than this one, investigate them.

## Installing

Unpack the file `elmr-distribution.tar.gz` on your filesystem. The directory structure will be a traditional Tomcat server tree:

    elmr
    ├── bin                (control scripts)
    ├── conf               (server wide and application configuration)
    ├── lib                (Tomcat server libraries)
    ├── logs
    ├── temp
    ├── webapps
    │   └── elmr
    │       └── WEB-INF
    │           └── lib    (elmr web application libraries)
    └── work

## Configuring

There are 7 configuration components:

1. The command line for system properties.
1. `bin/setenv.sh` for system properties.
1. `conf/server.xml` for Tomcat server-wide configuration.
1. `conf/context.xml` for elmr web application configuration (do not use `web.xml`).
1. `conf/logging.properties` for `java.util.logging`/`org.apache.tomcat.juli` configuration.
1. `conf/mod_jk.conf` for configuring the connection between Apache + Shibboleth and Tomcat.
1. `conf/workers.properties` for configuring the connection between Apache and Tomcat. 

It is recommended that web application configuration be set in the file `conf/context.xml`. However for ad-hoc runs it is fine to use the system properties set in `bin/setenv.sh` to override the values in the configuration file. System properties may also be set from the command line and this is a recommendation if running this service in a Docker container. System properties override all other settings named in other configuration files. Logging is configured in `conf/logging.properties`.

### Configuring Tomcat in conf/server.xml

See [Apache Tomcat 9 Configuration Reference, The Server Component](https://tomcat.apache.org/tomcat-9.0-doc/config/server.html) for details on editing this file. It should be very minimal and contain an [AJP connector](https://tomcat.apache.org/tomcat-9.0-doc/config/ajp.html) on port 8009 (if available, you can use other ports if needed). By default, an AJP connector on port 8009 is configured.

### Setting JAVA_HOME in bin/setenv.sh

If you are using a custom installation of Java-10 in a non-default location, set the `JAVA_HOME` environment variable in `bin/setenv.sh` to point to the base directory of your JDK or JRE install. See [RUNNING.TXT](https://tomcat.apache.org/tomcat-9.0-doc/RUNNING.txt) for other environment variables you can set.

### Setting System Properties in bin/setenv.sh or the Command Line

System properties may be set at startup and will override any other configuration that is set as described in the [subsequent subsections](#setting-context-parameters-in-confcatalinalocalhostelmrxml). They must be set in `bin/setenv.sh` by the `CATALINA_OPTS` environment variable. The table below lists what properties the application can accept outside the regular JVM system properties. See [RUNNING.TXT](https://tomcat.apache.org/tomcat-9.0-doc/RUNNING.txt) for other environment variables you can set.

Property | Description
---|---
`edu.illinois.techservices.elmr.AttributeMapReader.file`| Fully qualified path to a Shibboleth `attribute-map.xml` file. If not set, the value will fall back to a context parameter of the same name ([see below](#setting-context-parameters-in-confcatalinalocalhostelmrxml)).
`edu.illinois.techservices.elmr.SessionData.hostname` | Name of the host running an external datastore for storing attributes. If not set, the value will fall back to a context parameter of the same name ([see below](#setting-context-parameters-in-confcatalinalocalhostelmrxml)).
`edu.illinois.techservices.elmr.SessionData.port` | Port the external datastore is listening on. If not set, the value will fall back to a context parameter of the same name ([see below](#setting-context-parameters-in-confcatalinalocalhostelmrxml)).
`edu.illinois.techservices.elmr.SessionDataImpl.minConnections` | Minimum number of connections to the session data source to have initially in the pool. If not set, the value will fall back to a context parameter of the same name ([see below](#setting-context-parameters-in-confcatalinalocalhostelmrxml)).
`edu.illinois.techservices.elmr.SessionDataImpl.maxConnections` | Maximum number of connections to the session data source to have initially in the pool. If not set, the value will fall back to a context parameter of the same name ([see below](#setting-context-parameters-in-confcatalinalocalhostelmrxml)).
`edu.illinois.techservices.elmr.servlets.logoutUrl` | URL to your web ISO's logout. Can be an absolute or relative URL. If not set, the value will fall back to a context parameter of the same name ([see below](#setting-context-parameters-in-confcatalinalocalhostelmrxml)).
`edu.illinois.techservices.elmr.servlets.UniqueUserIdentifier` | Name of a request attribute that will be used as the initial unencoded value of the key for session data. If not set, the value will fall back to a context parameter of the same name ([see below](#setting-context-parameters-in-confcatalinalocalhostelmrxml)).
`edu.illinois.techservices.elmr.servlets.ApacheConfig` | Full path to an Apache configuration file containing `JkEnvVar` definitions. If not set, the value will fall back to a context parameter of the same name ([see below](#setting-context-parameters-in-confcatalinalocalhostelmrxml)).
`edu.illinois.techservices.elmr.servlets.DisableSecureCookies` | If `true`, any cookies set by elmr are not secure. This setting should only be used in development environments. If not set, the value will fall back to a context parameter of the same name ([see below](#setting-context-parameters-in-confcatalinalocalhostelmrxml)).
`edu.illinois.techservices.elmr.servlets.HtmlRenderer.formattedHtml` | When set to `true`, format output HTML in an indented readable format. This should only be set for development and debugging purposes.
`edu.illinois.techservices.elmr.servlets.HtmlRenderer.indentSpaces` | When set with the above property, use this value for the indent spaces. The default value is `2`.


### Setting Context Parameters in conf/context.xml

Context parameters are read when the Tomcat server is started from the `conf/context.xml` (there is no `webapps/elmr/WEB-INF/web.xml` file in this application). See [Tomcat Context Parameters](https://tomcat.apache.org/tomcat-9.0-doc/config/context.html#Context_Parameters) for how these work and how they replace elements in a traditional `web.xml` file. Edit the `value` attributes of the `<Parameter>` elements as follows:

Parameter Name | Description
---|---
`edu.illinois.techservices.elmr.AttributeMapReader.file`| Fully qualified path to a Shibboleth `attribute-map.xml` file. If not set, the value will fall back to a default value of `/etc/shibboleth/attribute-map.xml`.
`edu.illinois.techservices.elmr.SessionData.hostname` | Name of the host running an external datastore for storing attributes. If not set, the value will fall back to a default value of `localhost`.
`edu.illinois.techservices.elmr.SessionData.port` | Port the external datastore is listening on. If not set, the value will fall back to a default value of `6379`.
`edu.illinois.techservices.elmr.SessionDataImpl.minConnections` | Minimum number of connections to the session data source to have initially in the pool. If not set, the value will fall back to a default value of `0`.
`edu.illinois.techservices.elmr.SessionDataImpl.maxConnections` | Maximum number of connections to the session data source to have initially in the pool. If not set, the value will fall back to a default value of `8`.
`edu.illinois.techservices.elmr.servlets.ApacheConfig` | Full path to an Apache configuration file containing `JkEnvVar` definitions. If not set, the file will not load and elmr will not recognize that anything in Apache is configured.
`edu.illinois.techservices.elmr.servlets.DisableSecureCookies` | If `true`, any cookies set by elmr are not secure. This setting should only be used in development environments. If not set, the default is to use secure cookies.
`edu.illinois.techservices.elmr.servlets.logoutUrl` | URL to your web ISO's logout. Can be an absolute or relative URL. If this isn't set, logout will respond with a `500` status.
`edu.illinois.techservices.elmr.servlets.UniqueUserIdentifier` | Name of a request attribute that will be used as the initial unencoded value of the key for session data. If not set, the value will fall back to a default value of `Shib_Session_ID`.

These parameters would affect **ALL** contexts (web applications) deployed to the `webapps` directory. Since elmr is the only context, configuring at this level is OK. If you want to override this configuration or deploy more web applications, see the link above about configuring contexts in Tomcat.

### Configuring Logging in conf/logging.properties

Logging uses the Tomcat default logging system (which is based on the JDK logging system). See [Tomcat Logging](https://tomcat.apache.org/tomcat-9.0-doc/logging.html) and for details.

Loggers have been pre-configured to log at the highest level for each application package. Logs are configured by default to be written to `logs/localhost-yyyy-mm-dd.log` rolling them for 14 days. The application code will write some debugging and error messages to the log so they are useful for diagnosing issues during runtime.

### Configuring Apache HTTPD

There are 2 sample files you can use to configure `mod_jk`. You will be configuring attributes retrieved via `mod_shib` as environment variables. See the [Tomcat `mod_jk` documentation](https://tomcat.apache.org/connectors-doc/) for an overview of AJP and `mod_jk`.

It is recommended that `/elmr/session` and `/elmr/config` be configured to force Shibboleth authentication. The `/elmr/session` resource saves or destroys session data. The `/elmr/config` resource displays information about how elmr is configured and must not be visible to the general public.

#### conf/mod_jk.conf

Use the contents of this file to configure:

1. Which Shibboleth attributes to expose as `JkEnvVar`s.
1. Which paths will be routed through `mod_jk` as `JkMount`s.
1. Which paths will be protected via Shibboleth via `<Location>` directives.

#### conf/worker.properties

Copy this file to a location configured in your httpd's configuration. Edit as appropriate. See the [`workers.properties` reference](https://tomcat.apache.org/connectors-doc/reference/workers.html) for contents. For elmr, this ought to be a minimal configuration.

## Running

For general information about running a Tomcat server, see [RUNNING.TXT](https://tomcat.apache.org/tomcat-9.0-doc/RUNNING.txt). These instructions are provided here to get you started with the basic elmr configuration.

### Starting

Run the file `elmr/bin/startup.sh` to start the server. Tomcat will log messages to `elmr/logs/catalina.out` for startup and `elmr/logs/localhost-yyyy-mm-dd.log` about application startup and operation.

### Stopping

Run the file `elmr/bin/shutdown.sh` to stop the server. Tomcat will log messages to `elmr/logs/catalina.out` for shutdown.

## Troubleshooting

### Application Does Not Run

If the web application is not running, check `elmr/logs/catalina.out` for any log messages logged at `SEVERE` and look for anything related to `elmr` not starting. You will then check `elmr/logs/localhost-yyyy-mm-dd.log` for messages and stack traces for any unhandled exceptions. When errors occur, they will usually be when application listeners are started and data is being cached.

### Attributes Are Not Visible in the Application

If there are Shibboleth attributes that you expect to be visible in your application but are not showing up, you will have to review the JkEnvVars set in your Apache configuration and the attributes you set in your Shibboleth attribute map. These can be seen by visiting the `/elmr/config` page which will show what has been configured.

### Apache (not Tomcat) Responds to Requests with 413 Status

The [413 status](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/413) signals that Apache is trying to process a request containing an entity (header, attribute, etc) that exceeds its configured capacity. The likely cause in the case of Shibboleth is that one of the attributes contains **a lot** of data. Fix this by doing the following:

1. Edit `workers.properties` adding the line `worker.[worker-name].max_packet_size=65536`. Restart Apache.
1. Edit `elmr/conf/server.xml` adding the attribute `packetSize="65536"` to the AJP `<Connector>`. Restart Tomcat.

It is important that the values for `max_packet_size` and `packetSize` are the same. It's OK to set them this high. This isn't configured by default for any of the examples in the source. When this is done, the request will go through. 

See the [`workers.properties` reference](https://tomcat.apache.org/connectors-doc/reference/workers.html) and the [AJP Connector reference](https://tomcat.apache.org/tomcat-9.0-doc/config/ajp.html#Standard_Implementations) documentation for details.

## Etymology

The name is a play on a certain brand of glue found in school children's desks. It was chosen because the purpose for this server is mostly integration sometimes referred to as "glue" code.
