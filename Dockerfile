FROM openjdk:8-alpine

COPY target/uberjar/verse.jar /verse/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/verse/app.jar"]
