FROM openjdk:8-alpine
COPY target/fakesmtp-web-1.0.jar /opt/fakesmtp-web/
VOLUME ["/var/mail"]
ENV EMAIL_INPUT_DIR="/var/mail" \
    EMAIL_INPUT_DIR_POLL_RATE_SECONDS=5
EXPOSE 8080
ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/opt/fakesmtp-web/fakesmtp-web-1.0.jar"]