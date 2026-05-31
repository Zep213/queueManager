# QueueManager

Sistema de gestão de filas de atendimento presencial com notificações em tempo real via WebSocket.

---

## Stack Técnica

| Camada | Tecnologia |
|--------|------------|
| Backend | Java 21 + Spring Boot 4.0.0 |
| Banco de dados | MongoDB 7.0 |
| Tempo real | WebSocket (STOMP + SockJS) |
| Frontend | HTML5 + Bootstrap 5 + JavaScript |
| Segurança | Spring Security (RBAC) |
| Infraestrutura | Docker + Docker Compose |

---

## Perfis de Acesso

| Role | Acesso |
|------|--------|
| `ROLE_ADMIN` | Dashboard, relatórios, gestão de usuários |
| `ROLE_USER` | Mesa de atendimento (chamar, finalizar, cancelar senhas) |

Os usuários iniciais são criados automaticamente na primeira execução a partir das variáveis de ambiente definidas no `.env`.

---

## Configuração do Ambiente

### 1. Arquivo `.env`

Copie o `.env` na raiz do projeto e ajuste os valores antes de iniciar:

```env
SPRING_DATA_MONGODB_URI=mongodb://queue-mongo:27017/sistema-fila

ADMIN_USERNAME=admin
ADMIN_PASSWORD=SuaSenhaForteAqui!

ATENDENTE_USERNAME=atendente
ATENDENTE_PASSWORD=OutraSenhaForteAqui!

APP_CORS_ALLOWED_ORIGINS=http://localhost:8080
```

> O `.env` está no `.gitignore` — nunca é commitado.

### 2. Subir com Docker

```bash
docker-compose up --build
```

A aplicação ficará disponível em `http://localhost:8080`.  
O serviço `app` aguarda o MongoDB passar no healthcheck antes de iniciar.

### 3. Desenvolvimento local (sem Docker)

Ative o perfil `dev` que aponta para `localhost:27017`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Ou defina a variável diretamente:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.data.mongodb.uri=mongodb://localhost:27017/sistema-fila"
```

---

## Endpoints REST

### Públicos (sem autenticação)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/tickets` | Gerar nova senha (`?tipoTicket=NORMAL\|PRIORITARIO\|AVULSO&nomeCliente=...`) |
| `GET` | `/api/tickets/info-totem` | Dados para o totem (fila, previsão, vagas) |
| `GET` | `/api/tickets/fila/tamanho` | Quantidade de pessoas aguardando |

> `POST /api/tickets` tem rate limiting: máximo **30 requisições/minuto por IP**.

### Autenticados (`ROLE_USER` ou `ROLE_ADMIN`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/tickets` | Listar tickets ativos (AGUARDANDO + EM_ATENDIMENTO) |
| `POST` | `/api/tickets/proximo` | Chamar próximo da fila (prioridade: P → N → A) |
| `POST` | `/api/tickets/{id}/chamar` | Chamar senha específica |
| `PUT` | `/api/tickets/{id}/status` | Atualizar status (`?novoStatus=ATENDIDO\|CANCELADO`) |
| `PUT` | `/api/tickets/{id}/cancelar` | Cancelar atendimento |
| `POST` | `/api/tickets/pausa` | Arquivar finalizados e pausar |
| `GET` | `/api/tickets/historico` | Listar histórico (últimos 500, ordenados por data) |
| `GET` | `/api/tickets/historico/exportar` | Exportar histórico em CSV |
| `GET` | `/api/tickets/relatorio/mesas` | Relatório de atendimentos por mesa |

### Somente `ROLE_ADMIN`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/tickets/dashboard` | Dados em tempo real (fila + mesas ativas) |
| `GET` | `/api/usuarios` | Listar usuários |
| `POST` | `/api/usuarios` | Criar usuário (`{ username, password, role }`) |

---

## Transições de Status Permitidas

```
AGUARDANDO → EM_ATENDIMENTO | CANCELADO
EM_ATENDIMENTO → ATENDIDO | CANCELADO
ATENDIDO → (estado final)
CANCELADO → (estado final)
```

Tentativas de transição inválida retornam `400 Bad Request`.

---

## WebSocket

**Endpoint de conexão:** `ws://host/ws-queue` (com fallback SockJS)

**Tópico de eventos:** `/topic/senhas`

Todas as interfaces (totem, atendente, admin) assinam este tópico para receber atualizações em tempo real sempre que um ticket é criado, chamado ou cancelado.

---

## Tipos de Senha

| Prefixo | Tipo | Prioridade |
|---------|------|-----------|
| `P` | PRIORITARIO | 1ª (idosos, gestantes, PCD) |
| `N` | NORMAL | 2ª |
| `A` | AVULSO | 3ª (criada pelo atendente) |

---

## Rotinas Automáticas

- **00:00 diário:** Tickets com status `ATENDIDO` ou `CANCELADO` são movidos para o histórico (`tickets_historico`) e a sequência de numeração é resetada.

---

## Documentação Interativa da API

Com a aplicação rodando, acesse:

```
http://localhost:8080/swagger-ui.html
```

---

## Build Manual

```bash
# Compilar e gerar JAR
./mvnw clean package -DskipTests

# Executar
java -jar target/queueManager-0.0.1-SNAPSHOT.jar
```

---

## Testes

```bash
./mvnw test
```

Os testes unitários cobrem `TicketService`: criação de filas, priorização, transições de status e exceções customizadas (`TicketNotFoundException`, `FilaVaziaException`).

---

## Variáveis de Ambiente — Referência Completa

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `SPRING_DATA_MONGODB_URI` | URI de conexão com MongoDB | `mongodb://queue-mongo:27017/sistema-fila` |
| `ADMIN_USERNAME` | Login do usuário admin inicial | `admin` |
| `ADMIN_PASSWORD` | Senha do usuário admin inicial | `ChangeMe123!` |
| `ATENDENTE_USERNAME` | Login do atendente inicial | `atendente` |
| `ATENDENTE_PASSWORD` | Senha do atendente inicial | `ChangeMe123!` |
| `APP_CORS_ALLOWED_ORIGINS` | Origens CORS permitidas (vírgula para múltiplas) | `http://localhost:8080` |
| `app.atendimento.minutos-por-pessoa` | Minutos estimados por atendimento | `10` |
