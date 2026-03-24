FROM mcr.microsoft.com/openjdk/jdk:25-ubuntu

RUN apt-get update \
    && apt-get install -y --no-install-recommends wget ca-certificates tzdata gosu \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring --create-home --home-dir /home/spring spring \
    && mkdir -p /app/logs \
    && chown -R spring:spring /app /home/spring

WORKDIR /app

COPY --chown=spring:spring Server/target/*.jar /app/app.jar
COPY --chmod=755 docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

ENV APP_HOME=/app
ENV TZ=Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE=prod
ENV LOGGING_FILE_PATH=/app/logs
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70.0 -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:8081/actuator/health || exit 1

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
