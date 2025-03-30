FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . /app
RUN gradle clean bootJar --no-daemon

FROM amazoncorretto:21-alpine-jdk AS builder
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/authorization-microservice.jar
RUN java -Djarmode=layertools -jar authorization-microservice.jar extract

FROM amazoncorretto:21
WORKDIR /app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]