# Order Manager

API REST em **Java 17 / Spring Boot 3** para gerenciamento de **produtos** e **pedidos**, com autenticação via **JWT** e testes automatizados.

> Projeto de portfólio com foco em boas práticas: documentação, testes, segurança e organização do código.

## ✅ O que o sistema faz

- Cadastrar e gerenciar **produtos**
- Criar **pedidos** com lista de itens (produto + quantidade)
- Validar e **baixar estoque** automaticamente
- Calcular **valor total** do pedido
- Atualizar **status do pedido**
- Autenticar usuários com **JWT**
- Retornar erros em formato **JSON padronizado**
- Garantir comportamento com **testes de integração**

## ⚙️ Tecnologias

- Java 17
- Spring Boot 3 (Web, Validation, Data JPA, Security)
- JWT (jjwt)
- MySQL 8 + Flyway
- Maven
- Testes: JUnit 5 + Spring Boot Test + MockMvc
- OpenAPI/Swagger UI (springdoc)

## 📦 Estrutura do repositório (alto nível)

- `docker-compose.yml` na raiz (sobe app + banco)
- `backend/` (API Spring Boot)

Documentação detalhada da API: **`backend/backend-README.md`**.

## ▶️ Como rodar com Docker Compose (recomendado)

### 1) Preparar variáveis de ambiente

Na raiz do projeto:

~~~bash
cp .env.example .env
~~~

> Ajuste os valores no `.env` (principalmente `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` e `JWT_SECRET`).

### 2) Subir API + MySQL

~~~bash
docker compose up --build
~~~

A API ficará em:

- `http://localhost:8080`

Health check:

- `GET /health`

Swagger UI (se habilitado):

- `http://localhost:8080/swagger-ui/index.html`

> Observação: por padrão o MySQL **não expõe porta** para o host. Se você precisar acessar o banco fora do Docker, descomente a seção `ports` do serviço `db` no `docker-compose.yml`.

### Parar e limpar

~~~bash
docker compose down -v
~~~

## ▶️ Como rodar localmente (sem Docker)

Pré-requisitos:
- Java 17
- Maven
- MySQL 8 rodando localmente

### Banco (exemplo)

~~~sql
CREATE DATABASE order_manager;
CREATE USER 'order_user'@'localhost' IDENTIFIED BY 'ChangeMe123!';
GRANT ALL PRIVILEGES ON order_manager.* TO 'order_user'@'localhost';
FLUSH PRIVILEGES;
~~~

### Subir a API

Dentro de `backend/`:

~~~bash
mvn clean package
mvn spring-boot:run
~~~

Config padrão (pode sobrescrever via variáveis de ambiente):

- `DB_URL` (opcional)
- `DB_USER` (opcional)
- `DB_PASSWORD` (opcional)
- `PORT` (opcional)
- `JWT_SECRET` (**recomendado** sempre definir fora do repo)
- `JWT_EXPIRATION` (opcional)

## 🔐 Autenticação (JWT)

### Registro

`POST /api/auth/register`

~~~json
{
  "name": "Test User",
  "email": "teste@example.com",
  "password": "senha123"
}
~~~

### Login

`POST /api/auth/login`

~~~json
{
  "email": "teste@example.com",
  "password": "senha123"
}
~~~

Resposta (exemplo):

~~~json
{
  "token": "…",
  "type": "Bearer"
}
~~~

Usar o token nos endpoints protegidos:

`Authorization: Bearer <token>`

Rotas públicas:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /health`
- `/v3/api-docs/**`
- `/swagger-ui/**`

## ✅ Testes

Dentro de `backend/`:

~~~bash
mvn test
~~~

## 📚 Documentação detalhada

A documentação detalhada (regras, autorização, endpoints e exemplos) está em:

- `backend/backend-README.md`