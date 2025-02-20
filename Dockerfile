FROM ubuntu
COPY build/libs/*.jar app.jar
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk
EXPOSE 8090
CMD ["java", "-jar", "./app.jar"]