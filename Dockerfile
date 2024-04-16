# Stage 1: Build the application
FROM bellsoft/liberica-openjdk-alpine:21 as build
WORKDIR /app
COPY src ./src
COPY build.gradle.kts .
COPY dumpJsa.gradle.kts .
COPY gradlew .
COPY gradle ./gradle
RUN chmod +x ./gradlew
RUN --mount=type=cache,target=/root/.gradle ./gradlew build

# Stage 2: Run the application
FROM bellsoft/liberica-openjre-alpine:21 as run
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
