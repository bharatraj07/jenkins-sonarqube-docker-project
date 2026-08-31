FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/jenkins-sonarqube-project-1.0-SNAPSHOT.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
