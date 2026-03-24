#!/bin/sh
set -eu

APP_USER="${APP_USER:-spring}"
APP_GROUP="${APP_GROUP:-spring}"
LOG_DIR="${LOGGING_FILE_PATH:-/app/logs}"

mkdir -p "$LOG_DIR" "$LOG_DIR/normal" "$LOG_DIR/warn" "$LOG_DIR/error"

if [ "$(id -u)" = "0" ]; then
    chown -R "$APP_USER:$APP_GROUP" "$LOG_DIR"
    exec gosu "$APP_USER:$APP_GROUP" sh -c 'exec java $JAVA_OPTS -jar /app/app.jar'
fi

exec sh -c 'exec java $JAVA_OPTS -jar /app/app.jar'
