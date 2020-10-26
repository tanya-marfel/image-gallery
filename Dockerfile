FROM openjdk:11.0.9-jdk
ENV JAVA_OPTS=""
ARG JAR_FILE=build/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Ddb.host=gallery-db.cvqsnagksodq.eu-central-1.rds.amazonaws.com -jar app.jar"]
EXPOSE 8080