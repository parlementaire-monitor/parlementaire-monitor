# Stage 1: Cache Gradle dependencies
FROM gradle:jdk21 AS build
RUN mkdir -p /home/gradle
ENV GRADLE_USER_HOME=/home/gradle
COPY --chown=gradle:gradle build.gradle.kts gradle.properties settings.gradle.kts /home/gradle/
COPY --chown=gradle:gradle src /home/gradle/src
COPY --chown=gradle:gradle gradle /home/gradle/gradle
WORKDIR /home/gradle
RUN gradle installDist --no-daemon

# Stage 2: Build Application
FROM openjdk:21-bookworm
RUN apt update && apt upgrade -y && apt install -y curl vim
RUN useradd -M app
RUN mkdir -p /app /data/resources
RUN chown -R app:app /app /data
COPY --from=build --chown=app:app /home/gradle/build/install/ParlementaireMonitor /app/
RUN chmod +x /app/bin/ParlementaireMonitor

EXPOSE 8080
VOLUME ["/data/resources"]
HEALTHCHECK CMD ["/bin/bash", "-c", " curl http://localhost:8080/healthz || exit 1"]
ENTRYPOINT ["/app/bin/ParlementaireMonitor"]
