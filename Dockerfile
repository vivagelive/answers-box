FROM openjdk:11
EXPOSE 8080
ADD build/libs/*.jar answerBoxApp.jar
ENTRYPOINT ["java", "-jar", "answerBoxApp.jar"]
