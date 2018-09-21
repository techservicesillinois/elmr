FROM openjdk:10-jre-slim

MAINTAINER Technology Services, University of Illinois Urbana

ADD target/elmr-distribution.tar.gz /opt/

EXPOSE 8009
 
CMD  [ "java", "-cp",  "/opt/elmr/bin/bootstrap.jar:/opt/elmr/bin/tomcat-juli.jar", \
       "--add-opens=java.base/java.lang=ALL-UNNAMED", \
       "--add-opens=java.base/java.io=ALL-UNNAMED", \
       "--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED", \
       "-Djava.util.logging.config.file=/opt/elmr/conf/logging.properties", \
       "-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager", \
       "-Djdk.tls.ephemeralDHKeySize=2048", \
       "-Djava.protocol.handler.pkgs=org.apache.catalina.webresources", \
       "-Dorg.apache.catalina.security.SecurityListener.UMASK=0027", \
       "-Djava.security.egd=file:/dev/./urandom", \
       "-Dignore.endorsed.dirs=", \
       "-Dcatalina.base=/opt/elmr", \
       "-Dcatalina.home=/opt/elmr", \
       "-Djava.io.tmpdir=/opt/elmr/temp", \
       "org.apache.catalina.startup.Bootstrap", \
       "start" ]
