# Order Manager

API REST em **Java / Spring Boot** para gerenciamento de **produtos** e **pedidos** de uma loja simples, com autenticação via **JWT** e testes de integração.

O sistema permite:

- cadastrar e gerenciar produtos;
- criar pedidos com lista de itens (produto + quantidade);
- validar estoque automaticamente;
- calcular o valor total do pedido;
- atualizar o status do pedido;
- autenticar usuários com **JWT**;
- retornar erros em formato JSON padronizado;
- garantir o comportamento através de **testes de integração**.

> Projeto focado em estudos, estruturado para ser usado como **portfólio**.

---

## ⚙️ Tecnologias

- Java 17  
- Spring Boot 3  
- Spring Web  
- Spring Data JPA  
- Spring Security + JWT  
- Bean Validation (Jakarta Validation)  
- MySQL + Flyway  
- Maven  
- JUnit 5 + Spring Boot Test + MockMvc  

---

## 🧱 Estrutura (resumo)

    src/main/java/com/example/ordermanager
    ├── OrderManagerApplication.java
    ├── auth
    │   ├── SecurityConfig.java
    │   ├── JwtFilter.java
    │   ├── AuthController.java
    │   ├── User.java / Role.java
    │   └── UserDetailsServiceImpl.java
    ├── common
    │   └── ApiExceptionHandler.java
    ├── product
    │   ├── Product.java
    │   ├── dto/ProductDTO.java
    │   ├── ProductController.java
    │   ├── ProductService.java
    │   └── ProductRepository.java
    └── order
        ├── Order.java
        ├── OrderItem.java
        ├── OrderStatus.java
        ├── dto/
        │   ├── CreateOrderDTO.java
        │   ├── OrderItemResponseDTO.java
        │   └── OrderResponseDTO.java
        ├── OrderController.java
        ├── OrderService.java
        ├── OrderRepository.java
        └── OrderItemRepository.java

Migrations do banco:

    src/main/resources/db/migration
    └── V1__init.sql

---

## 🗄️ Banco de Dados

Exemplo de criação de banco/usuário no MySQL:

    CREATE DATABASE order_manager;
    CREATE USER 'order_user'@'localhost' IDENTIFIED BY 'ChangeMe123!';
    GRANT ALL PRIVILEGES ON order_manager.* TO 'order_user'@'localhost';
    FLUSH PRIVILEGES;

Configuração básica (`src/main/resources/application.properties`):

    spring.datasource.url=jdbc:mysql://localhost:3306/order_manager?useSSL=false&serverTimezone=UTC
    spring.datasource.username=order_user
    spring.datasource.password=ChangeMe123!

    spring.jpa.hibernate.ddl-auto=validate
    spring.jpa.show-sql=true

    spring.flyway.enabled=true

---

## ▶️ Como Rodar

Dentro da pasta `backend`:

1. Compilar:

       mvn clean package

2. Subir a aplicação:

       mvn spring-boot:run

A API ficará em:

    http://localhost:8080

Health check rápido:

    GET /health

---

## 🔐 Autenticação (JWT)

### Registro de usuário

    POST /api/auth/register
    Content-Type: application/json

    {
      "name":   "Test User",
      "email":  "teste@example.com",
      "password": "senha123"
    }

### Login

    POST /api/auth/login
    Content-Type: application/json

    {
      "email":  "teste@example.com",
      "password": "senha123"
    }

Resposta (exemplo):

    {
      "token": "<JWT_AQUI>",
      "type": "Bearer"
    }

Usar o token nos demais endpoints protegidos:

    Authorization: Bearer <JWT_AQUI>

Rotas públicas:

- POST /api/auth/register  
- POST /api/auth/login  
- GET  /health  

Todas as outras rotas exigem JWT válido.

---

## 📦 Produtos

### Regras

- CRUD completo.
- Paginação e filtro opcional por nome.
- Validações:
  - name: obrigatório, até 120 caracteres;
  - price: obrigatório, >= 0;
  - stock: obrigatório, >= 0.

### Endpoints

Listar (paginado):

    GET /api/products?page=0&size=10
    GET /api/products?name=mouse&page=0&size=10

Buscar por ID:

    GET /api/products/{id}

Criar:

    POST /api/products
    Authorization: Bearer <token>
    Content-Type: application/json

    {
      "name":  "Teclado Mecânico",
      "price": 250.00,
      "stock": 10
    }

Atualizar:

    PUT /api/products/{id}
    Authorization: Bearer <token>
    Content-Type: application/json

    {
      "name":  "Teclado Mecânico RGB",
      "price": 270.00,
      "stock": 8
    }

Deletar:

    DELETE /api/products/{id}
    Authorization: Bearer <token>

Erros comuns (corpo JSON):

- 404 – Product not found  
- 400 – Dados inválidos (campos com erro)

---

## 🧾 Pedidos

Um pedido é composto por:

- cabeçalho (`Order`): id, número, status, total, datas;
- itens (`OrderItem`): produto, quantidade, preço unitário, subtotal.

### Regras de negócio

- Ao criar pedido:
  - produto deve existir;
  - verificar **estoque suficiente**;
  - decrementar estoque dos produtos;
  - calcular valor total do pedido;
  - status inicial: `PENDING`.

- Atualização de status:  
  `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`.

### Endpoints

Criar pedido:

    POST /api/orders
    Authorization: Bearer <token>
    Content-Type: application/json

    {
      "items": [
        { "productId": 1, "quantity": 2 },
        { "productId": 2, "quantity": 1 }
      ]
    }

Possíveis respostas:

- 201 – criado com sucesso  
- 404 – produto não encontrado  

      { "error": "Produto não encontrado", "status": 404 }

- 409 – estoque insuficiente  

      { "error": "Estoque insuficiente para o produto: Teclado Mecânico", "status": 409 }

Buscar pedido por ID:

    GET /api/orders/{id}
    Authorization: Bearer <token>

Resposta (exemplo):

    {
      "id": 1,
      "orderNumber": "ORD-1763429365028",
      "status": "PENDING",
      "total": 650.00,
      "items": [
        {
          "productId": 1,
          "productName": "Teclado Mecânico",
          "quantity": 2,
          "unitPrice": 250.00,
          "subtotal": 500.00
        },
        {
          "productId": 2,
          "productName": "Mouse Gamer",
          "quantity": 1,
          "unitPrice": 150.00,
          "subtotal": 150.00
        }
      ]
    }

Listar pedidos (paginado):

    GET /api/orders?page=0&size=20
    Authorization: Bearer <token>

Atualizar status:

    PUT /api/orders/{id}/status?status=CONFIRMED
    Authorization: Bearer <token>

Cancelar / deletar:

    DELETE /api/orders/{id}
    Authorization: Bearer <token>

Se o pedido não existir:

    { "error": "Pedido não encontrado", "status": 404 }

---

## ❗ Tratamento de Erros

A classe `ApiExceptionHandler` centraliza o tratamento de exceções e devolve JSON padronizado, por exemplo:

    {
      "error": "Pedido não encontrado",
      "status": 404
    }

Para erros de validação:

    {
      "status": 400,
      "error": "Validation failed",
      "fields": {
        "name": "não pode ser nulo",
        "price": "deve ser maior ou igual a 0"
      }
    }

---

## 🧪 Testes

Executar testes:

    mvn test

Principais testes:

- `ProductControllerTest`
  - testa CRUD de produtos via MockMvc.
- `OrderControllerTest`
  - testa criação de pedidos;
  - erro de estoque insuficiente (HTTP 409);
  - busca de pedido por ID, etc.

Os testes usam `@SpringBootTest`, `@AutoConfigureMockMvc` e transações para isolar o estado.

---

## 🚀 Ideias de Evolução

- Documentação da API com Swagger (SpringDoc OpenAPI).
- Módulo de clientes (Customer) e relacionamento com pedidos.
- Filtros avançados na listagem de pedidos.
- Mais testes unitários e de integração.
- Dockerfile + docker-compose (app + MySQL).

---

## 📌 Observação

Projeto desenvolvido para estudo guiado (mentoria).  
Pode ser usado como **portfólio** no GitHub / LinkedIn e como base para entrevistas técnicas.
