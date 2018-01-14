#!/bin/bash
export FAKE_SMTP_WEB_API="http://192.168.48.128:60500"
docker rmi mjstewart/fakesmtp-web:1.0
cd src/main/ui
yarn run build
cd ../../../
mvn clean package -DskipTests
docker build -t mjstewart/fakesmtp-web:1.0 .
# docker-compose up -d
docker-compose up
