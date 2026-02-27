#!/bin/sh
AUTH_SERVICE_HOST="${AUTH_SERVICE_HOST:-auth-service}"
AUTH_IP=$(getent hosts "$AUTH_SERVICE_HOST" | awk '{ print $1 }')
export AUTH_SERVICE_URL="http://${AUTH_IP}:8081"
exec java $JAVA_OPTS -jar app.jar
