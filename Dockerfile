FROM openjdk:21
ENV SERVER_PORT 8090
ARG JAR_FILE
ADD target/${JAR_FILE} /final-customer-service.jar
ENTRYPOINT ["java", "-Duser.timezone=Asia/Taipei", "-jar", "/final-customer-service.jar"]