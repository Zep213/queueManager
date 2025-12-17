# 🎫 QueueManager - Sistema de Gerenciamento de Filas

Sistema simples e eficiente para gerenciamento de filas de atendimento presencial, desenvolvido com **Spring Boot** e **WebSocket** para atualizações em tempo real.

O sistema controla o fluxo desde a retirada da senha no Totem até o atendimento na Mesa e monitoramento pelo Gerente.

## 🚀 Tecnologias Utilizadas

* **Java 21**
* **Spring Boot 3** (Web, Security, WebSocket)
* **MongoDB** (Banco de dados NoSQL)
* **Thymeleaf** + **Bootstrap 5** (Frontend)
* **JavaScript (Vanilla)** (Lógica do cliente)

## ⚙️ Pré-requisitos

Antes de começar, você precisa ter instalado:
1.  [Java JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
2.  [Maven](https://maven.apache.org/) (ou usar o wrapper incluso `./mvnw`)
3.  [MongoDB](https://www.mongodb.com/try/download/community) rodando na porta padrão (`27017`).

## 🛠️ Como Rodar o Projeto

1.  **Clone o repositório**:
    ```bash
    git clone [https://github.com/seu-usuario/queuemanager.git](https://github.com/seu-usuario/queuemanager.git)
    cd queuemanager
    ```

2.  **Inicie o MongoDB**:
    Certifique-se de que o serviço do Mongo está ativo.

3.  **Execute a aplicação**:
    ```bash
    ./mvnw spring-boot:run
    ```
    Ou, se preferir, gere o JAR e execute:
    ```bash
    ./mvnw clean package
    java -jar target/queueManager-0.0.1-SNAPSHOT.jar
    ```

4.  **Acesse no Navegador**:
    * 🏠 **Totem (Público):** `http://localhost:8080`
    * 🔐 **Login (Restrito):** `http://localhost:8080/login.html`

## 🔑 Credenciais de Acesso (Padrão)

O sistema já vem com usuários pré-configurados (definidos em `securityConfig.java`):

| Perfil | Usuário | Senha | Acesso |
| :--- | :--- | :--- | :--- |
| **Gerente** | `admin` | `admin123` | Dashboard Completo (Fila + Mesas) |
| **Mesa 01** | `guiche01` | `user123` | Tela de Atendimento |
| **Mesa 02** | `guiche02` | `user123` | Tela de Atendimento |
| **Mesa 03** | `guiche03` | `user123` | Tela de Atendimento (Exemplo dinâmico) |

> **Nota:** Para criar novos usuários, adicione-os no `InMemoryUserDetailsManager` dentro da classe de configuração de segurança ou implemente um `UserDetailsService` conectado ao banco.

## 📡 Endpoints da API

O frontend comunica-se com o backend através de uma API REST e WebSocket.

### Principais Rotas
* `POST /api/tickets?tipoTicket=NORMAL&nomeCliente=Joao` - Gera nova senha.
* `GET /api/tickets/dashboard` - Retorna status da fila e mesas ativas (JSON).
* `POST /api/tickets/proximo` - Chama a próxima senha da fila.
* `PUT /api/tickets/{id}/status` - Atualiza status (ATENDIDO, CANCELADO).

### WebSocket
* Endpoint: `/ws-queue`
* Tópico de subscrição: `/topic/senhas` (Recebe atualizações sempre que a fila anda).

## 🧹 Limpeza Automática

O sistema possui um agendador (`AgendadorLimpeza.java`) que roda todo dia à meia-noite (`00:00`) para:
1.  Arquivar senhas atendidas/canceladas no histórico.
2.  Limpar a fila principal para o dia seguinte.

---
Desenvolvido para fins educacionais e práticos.