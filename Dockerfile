FROM eclipse-temurin:24-jdk AS builder

COPY .. /app
WORKDIR /app

RUN chmod 755 ./**

RUN ["./gradlew", "clean", "build", "-x", "test"]

FROM cgr.dev/chainguard/jre:latest AS runner

COPY --from=builder /app/build/libs/*.jar /arsha/app.jar
COPY --from=builder /app/config/application-default.yaml /arsha/config/application-default.yaml

WORKDIR /arsha

ENTRYPOINT ["java", "-jar", "app.jar"]
