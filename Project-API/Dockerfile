# ETAPA 1: BUILD (Usa a imagem Eclipse Temurin 24 com Maven)
# O Temurin costuma ter tags para as versões mais recentes.
FROM eclipse-temurin:24-jdk-alpine AS build

WORKDIR /app
COPY . /app/
# Compila o JAR usando o Maven que deve estar na imagem
RUN ./mvnw clean package -DskipTests

# ----------------------------------------------------------

# ETAPA 2: RUN (Usa a imagem Temurin 24 JRE mais leve)
FROM eclipse-temurin:24-jre-alpine

EXPOSE 8080
# Copia o JAR compilado da etapa 'build'
COPY --from=build /app/target/*.jar app.jar

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "/app.jar"]