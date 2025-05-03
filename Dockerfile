FROM amazoncorretto:20
ARG GITHUB_WORKSPACE
WORKDIR $GITHUB_WORKSPACE
RUN mkdir -p /app /data/resources
COPY ParlementaireMonitor-all.jar /app/ParlementaireMonitor-all.jar
RUN chown -R 1000:1000 /app /data
EXPOSE 8080
VOLUME ["/data/resources"]
USER 1000:1000
ENTRYPOINT ["java","-jar","/app/ParlementaireMonitor-all.jar"]
