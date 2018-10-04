FROM openjdk:10-jre-slim

ENV REDIS_PORT=6379 \
    LOGOUT=/auth/Shibboleth.sso/Logout

MAINTAINER Technology Services, University of Illinois Urbana

ADD target/elmr-distribution.tar.gz /opt/
RUN apt-get update && apt-get install -y \
      curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /etc/shibboleth \
    && mkdir -p /opt/elmr/work/Catalina/localhost/elmr \
    && chown -R root:root /opt/elmr/ \
    && chmod -R ugo+r /opt/elmr/  
COPY attribute-map.xml /etc/shibboleth/ 

USER nobody
EXPOSE 8009

HEALTHCHECK CMD curl -sS -o /dev/stderr -I -w "%{http_code}" http://localhost:8080/auth/elmr/attributes \
    | grep -q 302 || exit 1
 
ENTRYPOINT exec java -cp /opt/elmr/bin/bootstrap.jar:/opt/elmr/bin/tomcat-juli.jar \
       --add-opens=java.base/java.lang=ALL-UNNAMED \
       --add-opens=java.base/java.io=ALL-UNNAMED \
       --add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED \
       -Djava.util.logging.config.file=/opt/elmr/conf/logging.properties \
       -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager \
       -Djdk.tls.ephemeralDHKeySize=2048 \
       -Djava.protocol.handler.pkgs=org.apache.catalina.webresources \
       -Dorg.apache.catalina.security.SecurityListener.UMASK=0027 \
       -Djava.security.egd=file:/dev/./urandom \
       -Dignore.endorsed.dirs= \
       -Dcatalina.base=/opt/elmr \
       -Dcatalina.home=/opt/elmr \
       -Djava.io.tmpdir=/opt/elmr/temp \
       -Dedu.illinois.techservices.elmr.SessionData.hostname=$REDIS_HOSTNAME \
       -Dedu.illinois.techservices.elmr.SessionData.port=$REDIS_PORT \
       -Dedu.illinois.techservices.elmr.servlets.logoutUrl=$LOGOUT \
       org.apache.catalina.startup.Bootstrap \
       start
