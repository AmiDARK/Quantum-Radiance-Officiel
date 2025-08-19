# runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*-jar-with-dependencies.jar /app/radiance.jar
CMD ["java","-jar","/app/radiance.jar"]
