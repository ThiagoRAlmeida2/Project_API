# 📌 Kairos API

API desenvolvida em **Spring Boot** para gerenciamento de usuários, autenticação via **JWT**, perfis de **Aluno** e **Empresa**, e integração com banco de dados **MySQL**.  
Projeto criado como parte da residência do **Porto Digital**.

---

## 🚀 Tecnologias utilizadas

- **Java 24**
- **Spring Boot 3.5.5**
  - Spring Web (REST API)
  - Spring Data JPA (ORM)
  - Spring Security (Autenticação/Autorização)
- **JWT (JSON Web Token)** – `io.jsonwebtoken`
- **MySQL 8**
- **Lombok**
- **Maven**

---

## 📂 Estrutura principal

src/main/java/kairos/residencia/
├── model/ # Entidades JPA (Usuario, Aluno, Empresa, etc.)
├── repository/ # Repositórios JPA
├── security/ # JWT, filtros e configuração de segurança
├── controller/ # Endpoints REST
└── SrcApplication.java # Classe principal (entrypoint)

---

## ⚙️ Configuração do Banco de Dados

No arquivo `src/main/resources/application.properties` configure:

```properties
# Porta do servidor
server.port=8081

# Conexão com o MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/kairos_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT
app.jwt.secret=MinhaChaveSecretaSuperSegura123456
app.jwt.expiration-ms=86400000

spring.main.allow-bean-definition-overriding=true
📌 Certifique-se de criar o banco antes de rodar:
```
```bash 
CREATE DATABASE kairos_db;
```

▶️ Como rodar o projeto
Clone o repositório:

git clone https://github.com/ThiagoRAlmeida2/Project_API
cd kairos-api
Compile o projeto com Maven:

```bash

mvn clean package
## Rode a aplicação:

mvn spring-boot:run
Acesse:

http://localhost:8081
```

## 🔑 Autenticação
A API utiliza JWT. O fluxo é:

Usuário faz login (/auth/login) → retorna token JWT.

Token deve ser enviado no header Authorization:

```makefile
Authorization: Bearer <seu_token>
Dependendo do role (ROLE_ALUNO, ROLE_EMPRESA, ROLE_ADMIN), terá permissões diferentes.

📡 Endpoints principais
Método	Endpoint	Descrição	Permissão
POST	/auth/login	Autentica e retorna JWT	Público
POST	/usuarios	Cria um novo usuário	Público
GET	/usuarios/me	Retorna perfil do usuário autenticado	Autenticado
GET	/alunos	Lista alunos	ROLE_EMPRESA/Admin
GET	/empresas	Lista empresas	ROLE_ADMIN
```

## 👨‍💻 Equipe / Contribuidores
Back-end: Você 🎯

Front-end: Equipe parceira

Banco de dados: MySQL 8

## 📝 Notas
Use Java 24 (já configurado no pom.xml).

Certifique-se que o MySQL está rodando antes de iniciar a API.

Caso altere a porta (server.port), avise a equipe de front.

