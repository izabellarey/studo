# Zweistufiger Build: mit JDK bauen, mit JRE ausliefern.
#
# TODO 1 [Backend-Team]: Java-Version hier, in build.gradle und in der
#   CI-Pipeline konsistent halten. Aktuell 21.
#
# TODO 2 [Backend-Team]: Tests laufen im Image-Build nicht mit (-x test),
#   weil die CI sie ausführt. Prüfen, ob das so bleiben soll.
#
# TODO 3 [nach CI/CD-Tutorium 10.9.]: JVM-Speicheroptionen an das Testsystem
#   anpassen, sobald dessen Ausstattung bekannt ist.

# ---------- Build ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Laufzeit ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --create-home --shell /usr/sbin/nologin app
RUN mkdir -p /app/data && chown -R app:app /app/data
USER app

COPY --from=build /build/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]