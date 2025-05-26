# Ускоренный билд с кешем зависимостей
# Используйте BuildKit: docker build --progress=plain --no-cache=false .

# 1) Собираем в отдельном образе
FROM gradle:8.3-jdk17 AS builder
WORKDIR /home/gradle/project

# Копируем только манифесты Gradle и wrapper
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle/

# Предварительно скачиваем зависимости (закешируется)
RUN chmod +x gradlew \
 && ./gradlew dependencies --no-daemon

# Копируем исходники проекта
COPY src src/

# Собираем jar, при этом кеш зависимостей используется
RUN ./gradlew clean bootJar --no-daemon

# 2) Лёгкий рантайм-образ
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
