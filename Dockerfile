# ---------- STAGE 1: BUILD ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia só o pom primeiro (cache de dependências)
COPY pom.xml .
RUN mvn dependency:go-offline

# Agora copia o resto
COPY src ./src

# Build do jar
RUN mvn clean package -DskipTests

# ---------- STAGE 2: RUNTIME ----------
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copia o jar gerado
COPY --from=build /app/target/*.jar app.jar

# Porta padrão do Spring Boot
EXPOSE 8080

# Melhor prática para containers
ENTRYPOINT ["java", "-jar", "app.jar"]