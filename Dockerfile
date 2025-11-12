# ---------------------- STAGE 1: Build the JAR ----------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy pom.xml and download dependencies first
COPY pom.xml .
RUN mvn -B dependency:go-offline -DskipTests

# Copy the source code
COPY src ./src

# Package the app (skip tests for faster builds)
RUN mvn -B clean package -DskipTests

# ---------------------- STAGE 2: Runtime image ----------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy JAR from the build stage
COPY --from=build /workspace/target/*.jar app.jar

# Expose default Spring port
EXPOSE 8080

# JVM memory optimization (Render free tier = low memory)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Default command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
