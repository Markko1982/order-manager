# Execução local (Order Manager)

Este documento é a **fonte única de verdade** para executar o projeto localmente (API + banco).
Os READMEs da raiz e do backend devem **apenas apontar** para este arquivo quando o assunto for execução.

---

## Opção A — Docker Compose (recomendado)

### Pré-requisitos
- Docker + Docker Compose (comando `docker compose`)

### 1) Variáveis de ambiente

Na raiz do projeto:

```bash
cp .env.example .env
```

> Ajuste os valores no `.env` (principalmente `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` e `JWT_SECRET`).

Importante:
- **NUNCA commite** o arquivo `.env`.

### 2) Subir API + MySQL

Na raiz do projeto:

```bash
docker compose up --build
```

A API ficará em:
- `http://localhost:8080`

Health check:
- `GET /health`

Swagger UI (se habilitado):
- `http://localhost:8080/swagger-ui/index.html`

### 3) Parar e limpar (remove volume do banco)

```bash
docker compose down -v
```

### Acesso ao MySQL pelo host (opcional)

Por padrão, o MySQL **não expõe porta** para o host.
Se você precisar acessar o banco fora do Docker, descomente `ports` no serviço `db` em `docker-compose.yml`:

```yml
# ports:
#   - "3306:3306"
```

---

## Opção B — Rodar API no host (sem Docker)

### Pré-requisitos
- Java 17
- Maven (ou Maven Wrapper)
- MySQL 8 rodando localmente

### 1) Banco (exemplo)

```sql
CREATE DATABASE order_manager;
CREATE USER 'order_user'@'localhost' IDENTIFIED BY 'ChangeMe123!';
GRANT ALL PRIVILEGES ON order_manager.* TO 'order_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2) Variáveis de ambiente

A aplicação lê estas variáveis:
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `PORT` (opcional)

Exemplo:

```bash
export DB_URL="jdbc:mysql://localhost:3306/order_manager?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USER="order_user"
export DB_PASSWORD="ChangeMe123!"
export JWT_SECRET="dev-secret-change-me-please"
# opcional:
export PORT="8080"
```

#### Reutilizando o `.env` (opcional)

O `.env` da raiz foi pensado para o Docker Compose (`MYSQL_*`). Se quiser reaproveitar no host:

```bash
# a partir de backend/
set -a
source ../.env
set +a

export DB_URL="jdbc:mysql://localhost:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USER="${MYSQL_USER}"
export DB_PASSWORD="${MYSQL_PASSWORD}"
export JWT_SECRET="${JWT_SECRET}"
```

### 3) Subir a API

Dentro de `backend/`:

```bash
./mvnw spring-boot:run
```

---

## Testes (backend)

Os testes **precisam de acesso ao MySQL**.

1) Garanta MySQL disponível:
- localmente **ou**
- via Docker com a porta `3306` exposta (ver seção “Acesso ao MySQL pelo host”)

2) Garanta `DB_*` e `JWT_SECRET` definidos no ambiente.

3) Rode em `backend/`:

```bash
./mvnw test
```

---

## Troubleshooting rápido

- **Erro de conexão** (ex.: *Communications link failure*):
  - verifique se o MySQL está rodando e se o `DB_URL` está correto.
- **Access denied**:
  - confira `DB_USER` / `DB_PASSWORD`.
- **Porta 8080 em uso**:
  - rode com `export PORT=8081` antes do `spring-boot:run`.

Para diagnósticos mais detalhados: `backend/DIAGNOSTICO_ERROS.md`.

