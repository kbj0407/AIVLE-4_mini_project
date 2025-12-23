#!/bin/bash

APP_DIR=/home/ubuntu/app
JAR=$APP_DIR/miniproject04-0.0.1-SNAPSHOT.jar
LOG=$APP_DIR/app.log

pkill -f 'java -jar' || true

nohup java -jar "$JAR" > "$LOG" 2>&1 &

echo $! > $APP_DIR/app.pid
