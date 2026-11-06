# Etapa 1: compilar el jar con el JDK.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Primero solo los archivos de Gradle, para que las dependencias queden
# en cache y no se descarguen de nuevo cada vez que cambia el codigo.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q

COPY src src
RUN ./gradlew bootJar --no-daemon -q

# Etapa 2: imagen final, solo con el JRE y el jar.
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
USER app

COPY --from=build /app/build/libs/franquicias-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
