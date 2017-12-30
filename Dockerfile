FROM openjdk:9
COPY target/fakesmtp-web-0.0.1.jar /opt/fakesmtp-web
VOLUME ["/var/mail"]
ENV EMAIL_INPUT_DIR="/var/mail" \
    EMAIL_INPUT_DIR_POLL_RATE_SECONDS=1
EXPOSE 8080
ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/opt/fakesmtp-web/fakesmtp-web-0.0.1.jar"]

