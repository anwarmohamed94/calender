FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar calender.jar
ENTRYPOINT ["java","-jar","/calender.jar"]
EXPOSE 8081