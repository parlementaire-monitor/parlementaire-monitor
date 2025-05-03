FROM amazoncorretto:20
RUN mkdir -p /app /data/resources
COPY build/libs/*.jar /app/parlementaire-monitor.jar
RUN chown -R 1000:1000 /app /data
EXPOSE 8080
VOLUME ["/data/resources"]
USER 1000:1000
ENTRYPOINT ["java","-jar","/app/parlementaire-monitor.jar"]
