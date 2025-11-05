# Usa uma imagem base que já tem Java 21 e Maven
FROM maven:3.9.6-openjdk-21 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o código do seu projeto para o container
COPY . /app/

# Compila o projeto (cria o JAR)
RUN mvn clean package -DskipTests

# ==========================================================
# SEGUNDA ETAPA: RODA A APLICAÇÃO (Melhor e mais leve)
# ==========================================================

# Usa uma imagem mais leve que só tem o JRE para rodar o JAR
FROM openjdk:21-jre-slim

# Expõe a porta que o Spring Boot usa
EXPOSE 8080

# Copia o JAR compilado da etapa 'build'
COPY --from=build /app/target/*.jar app.jar

# Define o comando de inicialização
ENTRYPOINT ["java", "-jar", "/app.jar"]