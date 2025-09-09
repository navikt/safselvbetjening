FROM eclipse-temurin:21-jre AS builder
WORKDIR /build
COPY app/target/app.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --launcher --layers --destination extracted

FROM ghcr.io/navikt/baseimages/temurin:21
COPY --from=builder --chown=apprunner:apprunner /build/extracted/dependencies/ ./
COPY --from=builder --chown=apprunner:apprunner /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=apprunner:apprunner /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=apprunner:apprunner /build/extracted/application/ ./
COPY --chown=apprunner:apprunner --chmod=0755 run-java.sh /

ENV MAIN_CLASS="org.springframework.boot.loader.launch.JarLauncher"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -Djava.security.egd=file:/dev/./urandom \
               -Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true \
               -Dspring.profiles.active=nais"