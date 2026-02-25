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
- `docs/` (documentação de execução)

## ▶️ Execução local (single source of truth)

A execução local (com Docker Compose ou rodando a API no host), variáveis de ambiente e como rodar testes estão documentados em:

- `docs/EXECUCAO_LOCAL.md`

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

Como os testes dependem de banco (MySQL), os pré-requisitos e formas de execução estão em:

- `docs/EXECUCAO_LOCAL.md`

## 📚 Documentação detalhada

- Execução local (API + banco + testes): `docs/EXECUCAO_LOCAL.md`
- Documentação detalhada da API (regras, autorização, endpoints e exemplos): `backend/backend-README.md`

