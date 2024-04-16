# Stage 1: Build the application
#FROM --platform=linux/amd64 gradle:8.7.0-jdk21-alpine as build
FROM gradle:8.7.0-jdk21-alpine as build
WORKDIR /app
COPY build.gradle.kts .
COPY dumpJsa.gradle.kts .
COPY src ./src
RUN gradle build --no-daemon

# Stage 2: Run the application
FROM bellsoft/liberica-openjdk-alpine:21 as run
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
