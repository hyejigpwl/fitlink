FROM ubuntu

# JAR 파일 복사
COPY build/libs/FITLINK.jar /app/FITLINK.jar

# img 폴더 복사
COPY src/main/resources/static/img /usr/local/bin/img

# Java 설치
RUN apt-get update && apt-get install -y openjdk-17-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 컨테이너 내부에서 사용할 포트 설정
EXPOSE 8080

# JAR 실행
CMD ["java", "-jar", "/app/FITLINK.jar"]
