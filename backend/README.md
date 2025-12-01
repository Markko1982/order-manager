# Order Manager - API

Back-end em Java 17 + Spring Boot 3.
Autenticação com JWT. Banco MySQL via JPA/Flyway.

---

## Autenticação

### Registrar usuário

`POST /api/auth/register`

**Request**

```json
{
  "name": "Usuário Teste",
  "email": "usuario.teste@example.com",
  "password": "Senha123"
}

Response

201 Created (sem corpo)

Login

POST /api/auth/login

Request

{
  "email": "usuario.teste@example.com",
  "password": "Senha123"
}

Response

200 OK

{
  "token": "<JWT>",
  "type": "Bearer"
}

O token deve ser enviado em Authorization: Bearer <JWT> em todas as rotas protegidas.

Produtos
Criar produto

POST /api/products (requer JWT)

Request

{
  "name": "Produto Teste",
  "price": 10.50,
  "stock": 100
}

Response

201 Created

Corpo com o produto criado (id, name, price, stock, createdAt, updatedAt)

Pedidos
Criar pedido

POST /api/orders (requer JWT)

Request

{
  "items": [
    {
      "productId": 15,
      "quantity": 2
    }
  ]
}

Regras de validação

items não pode ser vazio.

quantity >= 1 e <= 50.

Produto precisa existir.

Estoque suficiente.

Response de sucesso

201 Created

Header Location: /api/orders/{id}

Corpo:

{
  "id": 7,
  "orderNumber": "ORD-...",
  "status": "PENDING",
  "total": 21.00,
  "items": [
    {
      "productId": 15,
      "productName": "Produto Teste",
      "quantity": 2,
      "unitPrice": 10.50,
      "subtotal": 21.00
    }
  ]
}

Erros possíveis

400 Bad Request (validação)

{
  "error": "Validation failed",
  "fields": {
    "items": "não deve estar vazio",
    "items[0].quantity": "deve ser menor que ou igual à 50"
  },
  "status": 400
}

409 Conflict (regra de negócio)

Estoque insuficiente:

{
  "error": "Estoque insuficiente para o produto 'Produto Teste'. Disponível: 98, solicitado: 9999",
  "status": 409
}


Valor máximo do pedido:

{
  "error": "Valor máximo do pedido excedido. Total calculado: 1029.00",
  "status": 409
}

Health Check
Health da aplicação

GET /health → público, retorna 200 com mensagem simples.

GET /api/health → protegido por JWT (conforme configuração atual).

Autorização (resumo)

/api/auth/**, /api/health, /health, Swagger/OpenAPI → permitAll().

Demais endpoints (/api/products, /api/orders, etc.) → requerem JWT válido.




