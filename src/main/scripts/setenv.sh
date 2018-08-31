# -----------------------------------------------------------------------------
#
# setenv.sh
#
# Sets environment variables as described in catalina.sh.
#
# -----------------------------------------------------------------------------


# -----------------------------------------------------------------------------
# CATALINA_OPTS
# -----------------------------------------------------------------------------

# Set the path to the attribute-map.xml file
#
#  Default is /etc/shibboleth/attribute-map.xml
#
# CATALINA_OPTS="-Dedu.illinois.techservices.elmr.AttributesMapReader.file=/path/to/attribute-map.xml"

# Set the entropy source to speed startup. See 
# https://wiki.apache.org/tomcat/HowTo/FasterStartUp#Entropy_Source for details
#
# CATALINA_OPTS="$CATALINA_OPTS -Djava.security.egd=file:/dev/./urandom"

# -----------------------------------------------------------------------------
# JAVA_HOME
# -----------------------------------------------------------------------------

# Set JAVA_HOME only if running from a custom java install location.
#
# JAVA_HOME=
