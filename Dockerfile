FROM openjdk:8-alpine3.7
COPY target/fakesmtp-web-1.2.jar /opt/fakesmtp-web/
COPY start.sh /
RUN set +x \
 && cd /opt/fakesmtp-web \
 && unzip fakesmtp-web-1.2.jar \
 && rm fakesmtp-web-1.2.jar \
 && chmod +x /start.sh \
 && cp BOOT-INF/classes/static/ui/bundle.js /

VOLUME ["/var/mail"]
ENV EMAIL_INPUT_DIR="/var/mail" \
    EMAIL_INPUT_DIR_POLL_RATE_SECONDS=10 \
    FAKE_SMTP_WEB_API=http://localhost:60500
EXPOSE 8080
ENTRYPOINT ["/start.sh"]
