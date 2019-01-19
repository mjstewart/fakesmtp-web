#!/bin/bash
export FAKE_SMTP_WEB_API="http://localhost:60500"
docker rmi mjstewart/fakesmtp-web:1.1
cd src/main/ui
yarn && yarn run build
cd ../../../
mvn clean package -DskipTests
docker build -t mjstewart/fakesmtp-web:1.1 .
docker-compose up -d
