# Contribuindo com o Order Manager

Obrigado por contribuir! Este repositório é um projeto em **Java 17 / Spring Boot 3** com foco em boas práticas (testes, segurança, organização e documentação).

## Fonte única de verdade (execução local e testes)
Toda a execução local (API + banco), variáveis de ambiente e como rodar testes estão documentados em:

- `docs/EXECUCAO_LOCAL.md`

> Se você for alterar algo relacionado à execução, atualize esse arquivo.

---

## Fluxo de trabalho (Git)

### 1) Atualize a `main`
- Garanta que você está na `main` atualizada e sem alterações pendentes.

### 2) Crie uma branch a partir da `main`
Padrão sugerido:
- `feature/<descricao-curta>` para novas funcionalidades
- `fix/<descricao-curta>` para correções
- `docs/<descricao-curta>` para documentação

Exemplos:
- `feature/pedidos-criar-endpoint`
- `fix/ajuste-validacao-email`
- `docs/melhora-execucao-local`

### 3) Commits
Preferimos mensagens claras, em PT-BR, no formato:

`<tipo>: <resumo>`

Tipos comuns: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`

Exemplos:
- `docs: adiciona guia de contribuição`
- `test: adiciona cenários de criação de pedido`
- `fix: corrige validação de estoque`

### 4) Antes do Pull Request
- Rode os testes do backend (`./mvnw test`) seguindo `docs/EXECUCAO_LOCAL.md`
- Revise o diff (`git diff`) e garanta que não há alterações acidentais
- Se houve mudança de banco, garanta uma migration nova em `backend/src/main/resources/db/migration`

---

## Padrões do projeto (o que esperamos nos PRs)

### Arquitetura e organização
- Prefira manter separação por camadas: **Controller → Service → Repository**
- Use DTOs para entrada/saída de API quando fizer sentido
- Validações devem ficar em DTOs (Bean Validation) e regras de negócio no Service

### Banco de dados (Flyway)
- Mudanças de schema devem vir com migration (`Vx__descricao.sql`)
- Migrações devem ser idempotentes e compatíveis com o histórico do projeto

### Erros e respostas
- Mantenha o padrão de retorno de erros já existente (handler centralizado)
- Mensagens e códigos devem ser consistentes e previsíveis para o cliente da API

### Testes
- Priorize testes automatizados para regras e endpoints
- Para endpoints novos/alterados, preferimos testes de integração (MockMvc)
- Se corrigiu bug, idealmente adicione um teste que quebre sem a correção

---

## Pull Request
Ao abrir PR, descreva:
- O que foi alterado e por quê
- Como testar (comandos e/ou exemplos de request)
- Riscos/impactos (ex.: migração, segurança, comportamento)

Obrigado!
