FROM eclipse-temurin:21-jre as builder
WORKDIR build
COPY app/target/app.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM ghcr.io/navikt/baseimages/temurin:21
COPY --from=builder --chown=apprunner:apprunner build/dependencies/ ./
COPY --from=builder --chown=apprunner:apprunner build/snapshot-dependencies/ ./
COPY --from=builder --chown=apprunner:apprunner build/spring-boot-loader/ ./
COPY --from=builder --chown=apprunner:apprunner build/application/ ./
COPY export-vault-secrets.sh /init-scripts/10-export-vault-secrets.sh

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=nais"