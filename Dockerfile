FROM azul/zulu-openjdk:21-latest AS builder

COPY .. /app
WORKDIR /app

RUN chmod 755 ./**

RUN ["./gradlew", "clean", "build", "-x", "test"]

FROM builder AS runner

RUN mkdir -p /arsha/config

COPY --from=builder /app/build/libs/*.jar /arsha/app.jar
COPY --from=builder /app/config/application-default.yaml /arsha/config/application-default.yaml

WORKDIR /arsha

ENTRYPOINT ["java", "-jar", "app.jar"]