# 🌐 Kairos API

API desenvolvida em **Spring Boot** para o gerenciamento de **usuários**, **autenticação JWT** e perfis de **Aluno** e **Empresa**, com integração ao banco de dados **MySQL**.

Projeto criado como parte da residência do **Porto Digital** 🧭.

---

## 🧾 Badges

![Java](https://img.shields.io/badge/Java-24-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Security-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build%20Tool-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

---

## 🚀 Tecnologias Utilizadas

- **Java 24**
- **Spring Boot 3.5.5**
    - Spring Web → Criação de APIs RESTful
    - Spring Data JPA → Mapeamento ORM
    - Spring Security → Autenticação e autorização
- **JWT (JSON Web Token)** — via `io.jsonwebtoken`
- **MySQL 8**
- **Lombok**
- **Maven**

---

## 🧩 Estrutura do Projeto

```
src/main/java/kairos/residencia/
├── controller/        # Endpoints REST
├── Dto/
├── model/             # Entidades JPA (Usuario, Aluno, Empresa, etc.)
├── repository/        # Interfaces de persistência (JPA)
├── response/
├── security/          # JWT, filtros e configurações de segurança
└── KairosApplication.java  # Classe principal (entrypoint)
```

---

## ⚙️ Configuração do Banco de Dados

No arquivo **`src/main/resources/application.properties`**, configure suas credenciais do MySQL:

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
```

📌 **Antes de rodar**, crie o banco de dados no MySQL:

```sql
CREATE DATABASE kairos_db;
```

---

## ▶️ Como Rodar o Projeto

Clone o repositório:

```bash
git clone https://github.com/ThiagoRAlmeida2/Project_API
```

Compile o projeto com Maven:

```bash
mvn clean package
```

Execute a aplicação:

```bash
mvn spring-boot:run
```

Acesse no navegador ou via API client:

👉 **http://localhost:8081**

---

## 🔐 Autenticação JWT

A autenticação é feita via **token JWT**.

### 🔄 Fluxo de Autenticação

1. O usuário faz login em `/auth/login`
2. A API valida as credenciais e retorna um token JWT
3. O token deve ser enviado no **header Authorization** para endpoints protegidos:

```http
Authorization: Bearer <seu_token>
```

---
### 🧰 Exemplo de Login (Request e Response)

**POST /auth/login**

#### 📥 Request
```json
{
  "email": "usuario@exemplo.com",
  "senha": "123456"
}
```

#### 📤 Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "expiraEm": "2025-11-01T12:00:00Z",
  "usuario": {
    "id": 1,
    "nome": "exempleAluno",
    "role": "ROLE_ALUNO"
  }
}
```

---

## 📄 Exemplo de Entidade: Usuario

```java
package kairos.residencia.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String role; 

    // perfil opcional
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Aluno aluno;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Empresa empresa;
}
```

---

## 📡 Endpoints Principais

| Método | Endpoint        | Descrição                                  | Permissão                 |
|:-------|:----------------|:-------------------------------------------|:---------------------------|
| POST   | `/auth/login`   | Autentica o usuário e retorna o token JWT  | Público                    |
| POST   | `/usuarios`     | Cria um novo usuário                       | Público                    |
| GET    | `/usuarios/me`  | Retorna o perfil do usuário autenticado    | Autenticado                |
| GET    | `/alunos`       | Lista todos os alunos                      | ROLE_EMPRESA / ROLE_ADMIN  |
| GET    | `/empresas`     | Lista todas as empresas                    | ROLE_ADMIN                 |

---

## 🧠 Boas Práticas

- Use **Java 24**, conforme definido no `pom.xml`.
- Verifique se o **MySQL** está rodando antes de iniciar a aplicação.
- Caso altere a porta (`server.port`), **informe a equipe de front-end**.
- Tokens JWT expiram após **24 horas** (configurável em `app.jwt.expiration-ms`).

---

## 👨‍💻 Equipe

| Função | Responsável |
|:-------|:-------------|
| Back-end | **Thiago Ribeiro** 🎯 |
| Front-end | Equipe Parceira |
| Banco de Dados | **MySQL 8** |

---

✨ **Desenvolvido com Java e café ☕ por [Thiago Ribeiro](https://github.com/ThiagoRAlmeida2)**