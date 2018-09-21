FROM openjdk:10-jre-slim

MAINTAINER Technology Services, University of Illinois Urbana

ADD target/elmr-distribution.tar.gz /opt/

EXPOSE 8009
 
ENTRYPOINT ["/opt/elmr/bin/startup.sh"]
