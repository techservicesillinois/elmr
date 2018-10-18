FROM golang:1.11-alpine as healthcheck

ENV SRC=healthcheck

RUN apk update && apk add git make upx

COPY $SRC/*.go $SRC/Makefile $GOPATH/src/
WORKDIR $GOPATH/src
RUN make deps && make && make compress

##############################################################
FROM maven:3-jdk-11-slim as builder

# Install strip so we may remove unnecessary debugging symbols from libraries
RUN apt-get update && apt-get install -y --no-install-recommends \
      binutils \
    && rm -rf /var/lib/apt/lists/*

## Build and install elmr into /tmp/fakeroot

WORKDIR /usr/src

COPY pom.xml /usr/src/
RUN mvn dependency:resolve

COPY src /usr/src/src/
RUN mvn package

RUN mkdir -p /tmp/fakeroot/opt \
    && tar xzf target/elmr-distribution.tar.gz -C /tmp/fakeroot/opt \
    && chmod -R oug+r /tmp/fakeroot/opt

## Build and install minimal JRE into /tmp/fakeroot

RUN jlink --compress=2 --output /tmp/fakeroot/opt/jre \
    --strip-debug --no-man-pages --no-header-files \
    --add-modules $(jdeps --print-module-deps \
        $(find /tmp/fakeroot/opt/elmr -type f -name \*.jar)) \
# Running strip on the jvm library cuts the size from 421M to 18M!
# https://github.com/docker-library/openjdk/issues/217
    && find /tmp/fakeroot/opt/jre -type f -execdir strip -p --strip-unneeded {} \;

# -------------------------------------------------------------------
# Collect desired files from manifest and determine necessary C libraries
COPY manifest /tmp
RUN ldd `cat /tmp/manifest` | grep -o '/.\+\.so[^ :]*' >> /tmp/manifest

RUN find /tmp/fakeroot/opt/jre -type f -execdir ldd {} \; | grep -o '/.\+\.so[^ :]*' | \
    grep -v '^/tmp/fakeroot' >> /tmp/manifest
RUN sort -u /tmp/manifest -o /tmp/manifest

RUN tar chf /tmp/root.tar -T /tmp/manifest
RUN mkdir -m 1777 /tmp/fakeroot/tmp \
    && mkdir -p /tmp/fakeroot/etc/shibboleth \
                /tmp/fakeroot/opt/elmr/work/Catalina/localhost/auth#elmr \
                /tmp/fakeroot/opt/elmr/conf/Catalina/localhost/auth#elmr

RUN tar xf /tmp/root.tar -C /tmp/fakeroot
COPY attribute-map.xml /tmp/fakeroot/etc/shibboleth/

###########################################################
FROM scratch

ENV REDIS_PORT=6379 \
    LOGOUT=/auth/Shibboleth.sso/Logout \
    DISABLE_SECURE_COOKIES=false

MAINTAINER Technology Services, University of Illinois Urbana

COPY --from=builder /tmp/fakeroot/ /
COPY --from=healthcheck /go/src/healthcheck /bin/

USER nobody
EXPOSE 8009

HEALTHCHECK CMD /bin/healthcheck -c 200 http://127.0.0.1:8009/auth/elmr/config -c 302 http://127.0.0.1:8009/auth/elmr/attributes

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
