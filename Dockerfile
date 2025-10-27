# Build stage
FROM maven:3.9.9-eclipse-temurin-24 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:24-jre
ENV TZ=UTC
RUN useradd -ms /bin/bash appuser
USER appuser
WORKDIR /app
COPY --from=build /workspace/target/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]