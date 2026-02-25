# Order Manager – API (Backend)

Back-end em **Java 17 + Spring Boot 3**, com:

- **Spring Web / Spring MVC**
- **Spring Security** com **JWT**
- **Spring Data JPA** + **Flyway** + **MySQL 8**
- **Springdoc / OpenAPI 3** para documentação
- Testes de integração com **MockMvc**

Este README é focado **somente na API** (`/backend`).  
Para visão geral do projeto (motivação, features, etc.), veja o `README.md` da **raiz do repositório**.

---

## 🔧 Arquitetura & Organização

Camadas principais:

- **Controller** (`controller`, `order`, `product`, `category`, `auth`): expõem os endpoints REST.
- **Service**: contém a regra de negócio (criar pedido, validar estoque, etc.).
- **Repository**: acesso a dados via Spring Data JPA.
- **Auth**: autenticação com JWT, modelagem de usuário e roles (`USER` / `ADMIN`).

Pacotes relevantes:

- `com.example.ordermanager.auth` – autenticação, JWT, usuários e configuração de segurança.
- `com.example.ordermanager.order` – domínio de pedidos.
- `com.example.ordermanager.product` – domínio de produtos.
- `com.example.ordermanager.category` – domínio de categorias.
- `com.example.ordermanager.controller` – controllers mais “globais” (ex.: health, pedidos).

---

## ⚙️ Configuração

### Banco de dados

A aplicação espera um MySQL acessível (por padrão local):

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/order_manager?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USER:order_user}
spring.datasource.password=${DB_PASSWORD:ChangeMe123!}
```

Você pode:

1. Criar o banco `order_manager` e o usuário `order_user` manualmente; ou  
2. Ajustar `application.properties` para usar seu usuário local.

As migrations do Flyway estão em:

```text
src/main/resources/db/migration
```

Arquivos atuais:

- `V1__init.sql`
- `V2__create_orders_and_order_items.sql`
- `V3__create_users_table.sql`
- `V4__add_category_to_products.sql`
- `V5__cascade_delete_order_items_on_product_delete.sql`

### Configuração de JWT

No arquivo `src/main/resources/application.properties`:

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:CHANGE_ME_DEV_ONLY}
jwt.expiration=${JWT_EXPIRATION:86400000}  # 24h em ms
```

> ⚠️ Em produção, essa chave deve vir de variável de ambiente e **nunca** ficar em texto plano no repositório.

---

## ▶️ Como rodar a API

Dentro da pasta `backend`:

```bash
mvn clean package

mvn spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

Health check simples:

```http
GET /health
```

Resposta esperada: string indicando que a API está funcionando.

---

## 🔐 Autenticação (JWT)

### Registro de usuário

```http
POST /api/auth/register
Content-Type: application/json
```

Body (exemplo):

```json
{
  "name": "João Silva",
  "email": "joao@example.com",
  "password": "123456"
}
```

Comportamento:

- Cria um usuário com role **`USER`** (padrão).
- E-mail é único (violação gera erro de validação / 400).
- Senha armazenada com **BCrypt**.

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "email": "joao@example.com",
  "password": "123456"
}
```

Resposta (exemplo):

```json
{
  "token": "<jwt-aqui>",
  "type": "Bearer"
}
```

Use o token no header `Authorization`:

```http
Authorization: Bearer <jwt-aqui>
```

---

## 👥 Perfis de usuário (roles)

Definidos em `auth/UserRole.java`:

- `USER`
- `ADMIN`

No registro via `/api/auth/register`, o usuário é criado como:

```java
user.setRole(UserRole.USER);
```

Usuários `ADMIN` podem ser criados:

- via script SQL / Flyway, ou  
- alterando manualmente a coluna `role` na tabela `users` em ambiente de desenvolvimento.

Na autenticação, o usuário autenticado expõe authorities como:

- `ROLE_USER` para `UserRole.USER`
- `ROLE_ADMIN` para `UserRole.ADMIN`

Essas roles são usadas pelo Spring Security nas expressões de autorização (`hasRole`, `hasAnyRole`).

---

## 🔒 Autorização – Visão Geral

Regras básicas configuradas em `auth/SecurityConfig.java`:

### Endpoints públicos (sem JWT)

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET  /health`
- Documentação OpenAPI/Swagger:
  - `/v3/api-docs/**`
  - `/swagger-ui/**`

### Demais endpoints

Demais recursos de negócio (produtos, pedidos, categorias) exigem **JWT válido**.  
Além disso, alguns endpoints têm **restrição por role**, via `@PreAuthorize` nos controllers.

---

## 🧩 Matriz de Autorização (resumo)

> ⚠️ Esta matriz reflete as regras atuais da API, baseada nas anotações `@PreAuthorize` e na configuração de segurança.

### Autenticação & Health

| Método | Caminho              | Auth    | Roles             | Descrição                           |
|--------|----------------------|---------|-------------------|-------------------------------------|
| POST   | `/api/auth/register` | Público | —                 | Registrar novo usuário (`USER`)     |
| POST   | `/api/auth/login`    | Público | —                 | Login e geração de JWT              |
| GET    | `/health`            | Público | —                 | Health check simples da aplicação   |

### Produtos (`/api/products`)

> As anotações `@PreAuthorize` no `ProductController` permitem leitura para usuários autenticados e restringem operações de escrita para `ADMIN`.

| Método | Caminho              | Auth | Roles permitidas    | Descrição                      |
|--------|----------------------|------|---------------------|--------------------------------|
| GET    | `/api/products`      | JWT  | `USER`, `ADMIN`     | Listar produtos (paginado)     |
| GET    | `/api/products/{id}` | JWT  | `USER`, `ADMIN`     | Buscar produto por ID          |
| POST   | `/api/products`      | JWT  | **`ADMIN` apenas**  | Criar produto                  |
| PUT    | `/api/products/{id}` | JWT  | **`ADMIN` apenas**  | Atualizar produto              |
| DELETE | `/api/products/{id}` | JWT  | **`ADMIN` apenas**  | Excluir produto                |

### Pedidos (`/api/orders`)

Regras de autorização implementadas no `OrderController`:

- Usuários autenticados (`USER` ou `ADMIN`) podem **criar** e **consultar** pedidos.
- Apenas `ADMIN` pode **alterar status** ou **cancelar/deletar** pedidos.

| Método | Caminho                    | Auth | Roles permitidas    | Descrição                          |
|--------|----------------------------|------|---------------------|------------------------------------|
| POST   | `/api/orders`              | JWT  | `USER`, `ADMIN`     | Criar novo pedido                  |
| GET    | `/api/orders`              | JWT  | `USER`, `ADMIN`     | Listar pedidos (paginado)          |
| GET    | `/api/orders/{id}`         | JWT  | `USER`, `ADMIN`     | Buscar pedido por ID               |
| PUT    | `/api/orders/{id}/status`  | JWT  | **`ADMIN` apenas**  | Atualizar status do pedido         |
| DELETE | `/api/orders/{id}`         | JWT  | **`ADMIN` apenas**  | Cancelar/Deletar pedido            |

### Categorias (`/api/categories`)

O controller de categorias não utiliza `@PreAuthorize` específico, então valem as **regras globais de segurança** (JWT obrigatório, sem distinção de role dentro da API).

| Método | Caminho                     | Auth | Roles permitidas    | Descrição                  |
|--------|-----------------------------|------|---------------------|----------------------------|
| GET    | `/api/categories`           | JWT  | `USER`, `ADMIN`     | Listar categorias          |
| GET    | `/api/categories/{id}`      | JWT  | `USER`, `ADMIN`     | Buscar categoria por ID    |
| POST   | `/api/categories`           | JWT  | `USER`, `ADMIN`     | Criar categoria            |
| PUT    | `/api/categories/{id}`      | JWT  | `USER`, `ADMIN`     | Atualizar categoria        |
| DELETE | `/api/categories/{id}`      | JWT  | `USER`, `ADMIN`     | Excluir categoria          |

> 💡 Em um sistema real, geralmente operações de escrita (POST/PUT/DELETE) em categorias ficam restritas a perfis administrativos.  
> Neste projeto de estudo, isso pode ser evoluído depois adicionando `@PreAuthorize` conforme a regra de negócio.

---

## 🧪 Testes

Para rodar os testes do backend:

```bash
cd backend
mvn test
```

Tipos de testes já presentes no projeto:

- Testes de controllers com **MockMvc**.
- Testes envolvendo **autenticação e autorização**:
  - uso de `@WithMockUser` com roles `USER` e `ADMIN`;
  - cenários de `403 Forbidden` para endpoints protegidos;
  - cenários `200 OK` / `204 No Content` para roles corretas.

---

## 💼 Fluxo de Trabalho Sugerido (dev backend)

Sugestão de fluxo de trabalho ao implementar uma nova feature:

1. Atualizar a branch base:

   ```bash
   git checkout main
   git pull
   ```

2. Criar uma branch de feature:

   ```bash
   git checkout -b feature/NOME-DA-FEATURE
   ```

3. Implementar a mudança (controller/service/repository/tests).
4. Rodar os testes:

   ```bash
   mvn test
   ```

5. Conferir arquivos alterados:

   ```bash
   git status
   ```

6. Adicionar apenas o que faz sentido para o commit:

   ```bash
   git add src/.../ArquivoAlterado.java
   ```

7. Criar um commit com mensagem clara (em português):

   ```bash
   git commit -m "feat: ajustar autorização de pedidos"
   ```

8. Enviar a branch:

   ```bash
   git push -u origin feature/NOME-DA-FEATURE
   ```

9. Abrir um Pull Request e descrever:
   - contexto,
   - o que foi alterado,
   - como testar,
   - riscos e próximos passos.

---

## 🧰 Dicas de Produtividade (VS Code + Terminal)

### Terminal (ripgrep)

Dentro de `backend`:

```bash
# procurar controllers de pedidos
rg "/api/orders" src/main/java

# encontrar a SecurityConfig
rg "SecurityConfig" src/main/java

# localizar anotações de segurança
rg "@PreAuthorize" src/main/java
```

### VS Code

- `Ctrl + P` → abrir arquivos rápido (`OrderController.java`, `SecurityConfig.java`, etc.).  
- `Ctrl + Shift + F` → busca global (equivalente visual do `rg`).  
- `Ctrl + Shift + O` → navegar entre métodos/símbolos do arquivo atual.

---

Este backend foi estruturado para estudo/mentoria, mas já segue práticas usadas em times reais:

- Migrations com Flyway.  
- Autenticação via JWT.  
- Autorização baseada em roles (`USER` / `ADMIN`).  
- Documentação por README + OpenAPI.  
- Testes cobrindo endpoints críticos e regras de segurança.

