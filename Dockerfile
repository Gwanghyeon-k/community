# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar -x test --no-daemon

RUN java -Djarmode=layertools -jar build/libs/*.jar extract

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
