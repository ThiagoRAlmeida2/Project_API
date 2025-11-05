# ETAPA 1: BUILD (Usa uma imagem com Java e Maven)
FROM maven:3.9.6-openjdk-21 AS build

WORKDIR /app
COPY . /app/
# Compila o JAR
RUN mvn clean package -DskipTests

# ----------------------------------------------------------

# ETAPA 2: RUN (Usa uma imagem mais leve com apenas o JRE)
FROM openjdk:21-slim-jre # <-- A TAG CORRIGIDA AQUI!

EXPOSE 8080
# Copia o JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "/app.jar"]