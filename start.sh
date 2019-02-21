#!/bin/sh
echo Set $FAKE_SMTP_WEB_API
cd /opt/fakesmtp-web
cp /bundle.js BOOT-INF/classes/static/ui/bundle.js
key=http:\\/\\/xxxxx
val=$(echo $FAKE_SMTP_WEB_API|sed s/\\//\\\\\\\//g)
sed -i "s/$key/$val/g" "BOOT-INF/classes/static/ui/bundle.js"
echo START ...
exec java -classpath . org.springframework.boot.loader.JarLauncher
