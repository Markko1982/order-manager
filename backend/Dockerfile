# Stage 1: build da aplicação
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos apenas o que é necessário para o Maven
COPY pom.xml .
COPY src ./src

# Build do JAR (sem rodar testes dentro do container por enquanto)
RUN mvn -B clean package -DskipTests \
    && cp target/*.jar app.jar

# Stage 2: imagem leve só com o JRE
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /app/app.jar app.jar

# Porta padrão da aplicação (pode ser sobrescrita por PORT)
EXPOSE 8080

# Permite passar opções extras de JVM via JAVA_OPTS se precisar
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
