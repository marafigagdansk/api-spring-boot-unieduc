# Finanças Simples API — Backend Spring Boot + PostgreSQL

API REST em **Java 17 + Spring Boot 4 + Spring Data JPA + PostgreSQL** para servir e persistir os dados da aplicação **Finanças Simples** (Web Vue.js e futuro aplicativo Android — Atividade Avaliativa A3 / UNICURITIBA).

---

## Pré-requisitos

- **Java JDK 17+** (com `JAVA_HOME` configurado)
- **Apache Maven 3.9+**
- **PostgreSQL 15+** em execução localmente ou remoto (configurado no `.env`)

---

## Configuração via `.env` (Porta da API e Banco PostgreSQL)

A aplicação lê automaticamente o arquivo `.env` na raiz do projeto (via `spring.config.import=optional:file:.env[.properties]`).

Copie [`.env.example`](./.env.example) para `.env` (já existe um `.env` pronto para desenvolvimento local) e ajuste os valores:

```env
# Porta HTTP em que a API Spring Boot será aberta ao ser executada
SERVER_PORT=8080

# Configuração do banco de dados PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=defaultdb
DB_USERNAME=postgres
DB_PASSWORD=sua_senha
DB_SSLMODE=require
DB_POOL_MAX_SIZE=10

# Origens permitidas no CORS (use * ou liste ex: http://localhost:5173)
CORS_ALLOWED_ORIGINS=*

# Se true, insere dados de demonstração automaticamente caso a tabela esteja vazia
APP_SEED_ON_EMPTY=false
```

> **Porta padrão da API:** `8080` (`http://localhost:8080/api/transactions`).  
> Caso altere `SERVER_PORT` no `.env` deste projeto, atualize também `VITE_API_URL` no `.env` do front-end (`educ-financ`).

> O script SQL inicial da tabela `transactions` está disponível em [`database/schema.sql`](./database/schema.sql) e o Hibernate (`ddl-auto=update`) também mantém o schema sincronizado.

---

## Como Executar

```bash
# Compilar e rodar os testes automatizados (usa banco H2 em memória para testes)
mvn test

# Iniciar a API Spring Boot (conectando ao MySQL e abrindo na porta definida em SERVER_PORT)
mvn spring-boot:run
```

---

## Endpoints Disponíveis (`/api/transactions`)

| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/transactions` | Lista todos os lançamentos (ordenados por data decrescente). Aceita `?type=income\|expense` e `?search=texto` opcionais. |
| `GET` | `/api/transactions/{id}` | Retorna os detalhes de um lançamento individual (`404` se não existir). |
| `POST` | `/api/transactions` | Cadastra um novo lançamento e retorna `201 Created`. |
| `PUT` | `/api/transactions/{id}` | Atualiza um lançamento existente e retorna `200 OK`. |
| `DELETE` | `/api/transactions/{id}` | Remove um lançamento pelo ID e retorna `{ "deletedId": id }`. |
| `POST` | `/api/transactions/import` | Valida e substitui os lançamentos atuais pelos enviados em JSON (`[...]` ou `{ "transactions": [...] }`). |
| `POST` | `/api/transactions/reset` | Restaura os 6 lançamentos iniciais de demonstração na tabela `transactions`. |

### Exemplo de Payload (`POST` / `PUT`)

```json
{
  "type": "expense",
  "amount": 480.50,
  "description": "Supermercado Mensal",
  "category": "Alimentação",
  "date": "2026-11-23"
}
```
