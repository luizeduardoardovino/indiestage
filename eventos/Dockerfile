# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2,sharing=locked chmod +x mvnw && ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*-SNAPSHOT.jar /app/app.jar
ENV SERVER_PORT=8080
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=90s --retries=12 CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/${SERVER_PORT:-8080}; printf "GET /actuator/health HTTP/1.0\r\nHost: localhost\r\n\r\n" >&3; read -r -t 4 status <&3; [[ "$status" == *" 200 "* ]]'
ENTRYPOINT ["java", "-jar", "/app/app.jar"]