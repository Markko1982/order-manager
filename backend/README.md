# Order Manager - Sistema de Gerenciamento de Pedidos

## 📋 Descrição
Projeto Java Spring Boot de treinamento para gerenciamento de pedidos e produtos.

## 🛠️ Tecnologias Utilizadas
- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Data JPA**
- **Spring Validation**
- **MySQL**
- **Flyway** (migrations de banco de dados)
- **Maven**

## ✅ Correções Aplicadas

### Problemas Resolvidos
1. **Dependência de Validação**: Adicionada a dependência `spring-boot-starter-validation` que estava faltando
2. **Classe Desnecessária**: Removida a classe `App.java` que não era utilizada
3. **Java 17**: Configurado ambiente com JDK 17

## 📦 Estrutura do Projeto

```
order-manager/
├── src/main/
│   ├── java/com/example/ordermanager/
│   │   ├── OrderManagerApplication.java      # Classe principal
│   │   ├── common/
│   │   │   └── ApiExceptionHandler.java      # Tratamento global de exceções
│   │   ├── controller/
│   │   │   └── HealthController.java         # Endpoint de health check
│   │   └── product/
│   │       ├── Product.java                  # Entidade JPA
│   │       ├── ProductController.java        # REST Controller
│   │       ├── ProductRepository.java        # Repository JPA
│   │       ├── ProductService.java           # Lógica de negócio
│   │       └── dto/
│   │           └── ProductDTO.java           # Data Transfer Object
│   └── resources/
│       ├── application.properties            # Configurações da aplicação
│       └── db/migration/
│           └── V1__init.sql                  # Migration inicial do banco
└── pom.xml                                   # Dependências Maven
```

## 🚀 Como Executar

### Pré-requisitos
- Java 17 ou superior
- MySQL 8.0 ou superior
- Maven 3.6 ou superior

### 1. Configurar o Banco de Dados

Crie o banco de dados e o usuário no MySQL:

```sql
CREATE DATABASE order_manager;
CREATE USER 'order_user'@'localhost' IDENTIFIED BY 'ChangeMe123!';
GRANT ALL PRIVILEGES ON order_manager.* TO 'order_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configurar application.properties

Edite o arquivo `src/main/resources/application.properties` se necessário:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/order_manager?useSSL=false&serverTimezone=UTC
spring.datasource.username=order_user
spring.datasource.password=ChangeMe123!
```

### 3. Compilar o Projeto

```bash
mvn clean package
```

### 4. Executar a Aplicação

```bash
java -jar target/order-manager-0.0.1-SNAPSHOT.jar
```

Ou usando Maven:

```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

## 📡 Endpoints da API

### Health Check
```
GET /health
```
Retorna o status da aplicação.

### Produtos

#### Listar Produtos (com paginação)
```
GET /api/products?page=0&size=10
GET /api/products?name=produto&page=0&size=10
```

#### Buscar Produto por ID
```
GET /api/products/{id}
```

#### Criar Produto
```
POST /api/products
Content-Type: application/json

{
  "name": "Notebook Dell",
  "price": 3500.00,
  "stock": 10
}
```

#### Atualizar Produto
```
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "Notebook Dell Atualizado",
  "price": 3200.00,
  "stock": 15
}
```

#### Deletar Produto
```
DELETE /api/products/{id}
```

## 🔍 Validações

O sistema valida automaticamente os dados de entrada:

- **name**: Obrigatório, máximo 120 caracteres
- **price**: Obrigatório, deve ser >= 0
- **stock**: Obrigatório, deve ser >= 0

## 🗄️ Banco de Dados

O Flyway gerencia automaticamente as migrations do banco de dados. A tabela `products` é criada automaticamente na primeira execução.

### Estrutura da Tabela Products

| Campo      | Tipo           | Descrição                    |
|------------|----------------|------------------------------|
| id         | BIGINT         | Chave primária (auto-increment) |
| name       | VARCHAR(120)   | Nome do produto              |
| price      | DECIMAL(15,2)  | Preço do produto             |
| stock      | INT            | Quantidade em estoque        |
| created_at | TIMESTAMP(6)   | Data de criação              |
| updated_at | TIMESTAMP(6)   | Data de atualização          |

## 🧪 Testes

Para executar os testes:

```bash
mvn test
```

## 📝 Próximos Passos Sugeridos

1. **Implementar módulo de Pedidos (Orders)**
   - Criar entidade Order
   - Relacionamento com Product
   - Endpoints CRUD para pedidos

2. **Implementar módulo de Clientes (Customers)**
   - Criar entidade Customer
   - Relacionamento com Order
   - Endpoints CRUD para clientes

3. **Adicionar Autenticação e Autorização**
   - Spring Security
   - JWT tokens
   - Roles e permissões

4. **Implementar Testes Unitários e de Integração**
   - JUnit 5
   - Mockito
   - TestContainers para testes com MySQL

5. **Adicionar Documentação da API**
   - SpringDoc OpenAPI (Swagger)
   - Documentação interativa

6. **Implementar Cache**
   - Spring Cache
   - Redis

7. **Adicionar Logs Estruturados**
   - Logback
   - ELK Stack

## 🤝 Contribuindo

Este é um projeto de treinamento. Sinta-se livre para fazer fork e experimentar!

## 📄 Licença

Projeto de treinamento - uso livre para fins educacionais.
