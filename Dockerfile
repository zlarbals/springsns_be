FROM openjdk:11-jdk-slim

ARG JAR_FILE=target/*.jar
ADD ${JAR_FILE} spring-sns.jar

ENTRYPOINT ["java","-jar","spring-sns.jar"]