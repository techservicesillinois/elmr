FROM maven:3-jdk-11-slim as builder

# Install strip so we may remove unnecessary debugging symbols from libraries
RUN apt-get update && apt-get install -y --no-install-recommends \
      binutils \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src

COPY pom.xml /usr/src/
RUN mvn dependency:resolve

COPY src /usr/src/src/
RUN mvn package

RUN mkdir /tmp/dist \
    && tar xzf target/elmr-distribution.tar.gz -C /tmp/dist

RUN jlink --compress=2 --output /tmp/jre \
    --strip-debug --no-man-pages --no-header-files \
    --add-modules \
    java.base,java.desktop,java.instrument,java.logging,java.management,java.naming,java.security.jgss,java.scripting,java.sql,java.xml \
# Running strip on the jvm library cuts the size from 421M to 18M!
# https://github.com/docker-library/openjdk/issues/217
    && find /tmp/jre -type f -execdir strip -p --strip-unneeded {} \;

###########################################################
FROM debian:sid-slim

ENV REDIS_PORT=6379 \
    LOGOUT=/auth/Shibboleth.sso/Logout \
    DISABLE_SECURE_COOKIES=false

MAINTAINER Technology Services, University of Illinois Urbana

COPY --from=builder /tmp/jre /opt/jre
COPY --from=builder /tmp/dist/ /opt/

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

ENTRYPOINT exec /opt/jre/bin/java -cp /opt/elmr/bin/bootstrap.jar:/opt/elmr/bin/tomcat-juli.jar \
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
       -Dedu.illinois.techservices.elmr.servlets.DisableSecureCookies=$DISABLE_SECURE_COOKIES \
       org.apache.catalina.startup.Bootstrap \
       start
