# 🚀 Próximos Passos - Order Manager

## Roadmap de Desenvolvimento Sugerido

---

## 📦 Fase 1: Módulo de Pedidos (Orders)

### Objetivo
Implementar o gerenciamento completo de pedidos, permitindo criar, listar, atualizar e cancelar pedidos.

### Tarefas

#### 1.1 Criar Entidade Order
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String orderNumber;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
    private Instant createdAt;
    private Instant updatedAt;
}
```

#### 1.2 Criar Entidade OrderItem
```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
```

#### 1.3 Criar Migration
```sql
-- V2__create_orders.sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

#### 1.4 Implementar Camadas
- `OrderRepository` extends JpaRepository
- `OrderService` com lógica de negócio
- `OrderController` com endpoints REST
- `OrderDTO` e `CreateOrderDTO`

#### 1.5 Endpoints Sugeridos
```
POST   /api/orders              - Criar pedido
GET    /api/orders              - Listar pedidos (paginado)
GET    /api/orders/{id}         - Buscar pedido por ID
PUT    /api/orders/{id}/status  - Atualizar status do pedido
DELETE /api/orders/{id}         - Cancelar pedido
```

---

## 👥 Fase 2: Módulo de Clientes (Customers)

### Objetivo
Gerenciar informações de clientes e associá-los aos pedidos.

### Tarefas

#### 2.1 Criar Entidade Customer
```java
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    private String phone;
    private String cpf;
    
    @OneToMany(mappedBy = "customer")
    private List<Order> orders;
    
    @Embedded
    private Address address;
    
    private Instant createdAt;
    private Instant updatedAt;
}
```

#### 2.2 Criar Migration
```sql
-- V3__create_customers.sql
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(20),
    cpf VARCHAR(14) UNIQUE,
    street VARCHAR(200),
    number VARCHAR(10),
    complement VARCHAR(100),
    neighborhood VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(2),
    zip_code VARCHAR(10),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

ALTER TABLE orders ADD COLUMN customer_id BIGINT;
ALTER TABLE orders ADD FOREIGN KEY (customer_id) REFERENCES customers(id);
```

#### 2.3 Endpoints Sugeridos
```
POST   /api/customers           - Criar cliente
GET    /api/customers           - Listar clientes (paginado)
GET    /api/customers/{id}      - Buscar cliente por ID
PUT    /api/customers/{id}      - Atualizar cliente
DELETE /api/customers/{id}      - Deletar cliente
GET    /api/customers/{id}/orders - Listar pedidos do cliente
```

---

## 🔐 Fase 3: Autenticação e Autorização

### Objetivo
Implementar segurança na API com autenticação JWT e controle de acesso baseado em roles.

### Tarefas

#### 3.1 Adicionar Dependências
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

#### 3.2 Criar Entidade User
```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;
    private String email;
    
    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN, MANAGER, USER
    
    private boolean enabled;
}
```

#### 3.3 Implementar
- `JwtTokenProvider` - Geração e validação de tokens
- `JwtAuthenticationFilter` - Filtro de autenticação
- `SecurityConfig` - Configuração de segurança
- `AuthController` - Endpoints de login/registro

#### 3.4 Endpoints de Autenticação
```
POST /api/auth/register  - Registrar novo usuário
POST /api/auth/login     - Login e obter token JWT
POST /api/auth/refresh   - Renovar token
```

---

## 🧪 Fase 4: Testes Automatizados

### Objetivo
Garantir qualidade do código com testes unitários e de integração.

### Tarefas

#### 4.1 Adicionar Dependências
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

#### 4.2 Implementar Testes

**Testes Unitários:**
- `ProductServiceTest` - Testes da lógica de negócio
- `OrderServiceTest` - Testes de criação e validação de pedidos
- `CustomerServiceTest` - Testes de gerenciamento de clientes

**Testes de Integração:**
- `ProductControllerIntegrationTest` - Testes de endpoints
- `OrderControllerIntegrationTest` - Testes de fluxo completo
- `AuthenticationIntegrationTest` - Testes de autenticação

#### 4.3 Configurar CI/CD
- GitHub Actions para executar testes automaticamente
- Cobertura de código com JaCoCo
- Quality gate com SonarQube

---

## 📚 Fase 5: Documentação da API

### Objetivo
Criar documentação interativa da API usando OpenAPI/Swagger.

### Tarefas

#### 5.1 Adicionar Dependência
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

#### 5.2 Configurar
```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
```

#### 5.3 Adicionar Anotações
- `@Operation` - Descrever endpoints
- `@ApiResponse` - Documentar respostas
- `@Schema` - Documentar DTOs

#### 5.4 Acessar Documentação
```
http://localhost:8080/swagger-ui.html
http://localhost:8080/api-docs
```

---

## 🚀 Fase 6: Performance e Cache

### Objetivo
Melhorar performance da aplicação com cache e otimizações.

### Tarefas

#### 6.1 Adicionar Redis
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

#### 6.2 Configurar Cache
```java
@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configuração do cache
    }
}
```

#### 6.3 Aplicar Cache
```java
@Cacheable("products")
public Product get(Long id) { ... }

@CacheEvict(value = "products", allEntries = true)
public Product update(Long id, ProductDTO dto) { ... }
```

---

## 📊 Fase 7: Monitoramento e Logs

### Objetivo
Implementar observabilidade com logs estruturados e métricas.

### Tarefas

#### 7.1 Adicionar Actuator
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

#### 7.2 Configurar Endpoints
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
```

#### 7.3 Implementar Logs Estruturados
- Usar SLF4J com Logback
- Formato JSON para logs
- Integração com ELK Stack (Elasticsearch, Logstash, Kibana)

#### 7.4 Adicionar Métricas
- Micrometer para métricas
- Integração com Prometheus
- Dashboards no Grafana

---

## 🐳 Fase 8: Containerização

### Objetivo
Facilitar deploy com Docker e Docker Compose.

### Tarefas

#### 8.1 Criar Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/order-manager-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 8.2 Criar docker-compose.yml
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: order_manager
      MYSQL_USER: order_user
      MYSQL_PASSWORD: ChangeMe123!
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3306:3306"
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/order_manager
```

---

## 📱 Fase 9: Frontend (Opcional)

### Objetivo
Criar interface web para consumir a API.

### Sugestões de Tecnologia
- **React** + TypeScript + Vite
- **Vue.js** 3 + Composition API
- **Angular** 17+

### Funcionalidades
- Dashboard com estatísticas
- CRUD de produtos
- Gerenciamento de pedidos
- Gerenciamento de clientes
- Autenticação e autorização

---

## 🎯 Priorização Recomendada

### Curto Prazo (1-2 semanas)
1. ✅ Módulo de Pedidos (Orders)
2. ✅ Módulo de Clientes (Customers)
3. ✅ Testes Unitários Básicos

### Médio Prazo (1 mês)
4. ✅ Autenticação e Autorização
5. ✅ Documentação Swagger
6. ✅ Testes de Integração

### Longo Prazo (2-3 meses)
7. ✅ Cache e Performance
8. ✅ Monitoramento e Logs
9. ✅ Containerização
10. ✅ Frontend (Opcional)

---

## 📖 Recursos de Aprendizado

### Documentação Oficial
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security](https://spring.io/projects/spring-security)

### Tutoriais Recomendados
- Baeldung - Spring Boot Tutorials
- Spring Academy - Cursos gratuitos
- YouTube - Michelli Brito, DevDojo

### Livros
- "Spring Boot in Action" - Craig Walls
- "Spring Microservices in Action" - John Carnell

---

**Boa sorte no desenvolvimento! 🚀**
