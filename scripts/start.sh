#!/bin/bash

APP_DIR=/home/ubuntu/app
JAR=$(ls $APP_DIR/*.jar | head -n 1)

nohup java -jar $JAR > $APP_DIR/app.log 2>&1 &
