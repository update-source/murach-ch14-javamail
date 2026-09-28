#!/bin/sh
set -e
# Make Tomcat's HTTP connector listen on $PORT (Render sets this variable).
sed -i "s/port=\"8080\"/port=\"${PORT:-8080}\"/" /usr/local/tomcat/conf/server.xml
exec catalina.sh run
