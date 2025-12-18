```markdown
# QueueManager

**Versão:** 1.0.0 (Stable)
**Status:** Produção

Sistema corporativo de gestão de filas e atendimento presencial com processamento em tempo real. Solução completa projetada para alta disponibilidade, segurança e previsibilidade de fluxo de atendimento.

---

## 📋 Especificações Técnicas

O projeto segue uma arquitetura moderna baseada em microsserviços e comunicação assíncrona.

* **Core:** Java 21 (LTS) / Spring Boot 3
* **Database:** MongoDB (NoSQL) - Otimizado para alta volumetria de logs.
* **Real-time:** WebSocket (Protocolo STOMP) - Sincronização instantânea entre terminais.
* **Frontend:** Thymeleaf + Bootstrap 5 (Renderização Server-Side).
* **Security:** Spring Security com controle de acesso baseado em funções (RBAC).

## ⚙️ Requisitos de Ambiente

Para implantação (deploy) em ambiente de produção:

1.  **Java JDK 21** ou superior configurado no PATH.
2.  **MongoDB** (v5.0+) operando na porta padrão `27017` ou via string de conexão externa.
3.  **Maven** (apenas para build/compilação).

## 🚀 Instalação e Build

Este é um software proprietário. Certifique-se de que o ambiente possui as credenciais SSH necessárias para clonagem.

**1. Clonagem e Compilação**
```bash
git clone git@github.com:sua-empresa/queuemanager.git
cd queuemanager
./mvnw clean package -DskipTests

```

**2. Execução do Artefato**
Recomendamos a execução via JAR com perfil de produção ativo.

```bash
java -jar target/queueManager-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

```

## 🔐 Controle de Acesso e Segurança

O sistema implementa uma camada de segurança robusta para proteger as operações de atendimento.

**Perfis de Acesso:**

* `ROLE_ADMIN`: Acesso irrestrito a dashboards, relatórios gerenciais e monitoramento de filas.
* `ROLE_USER`: Acesso operacional para mesas de atendimento e triagem.

> ⚠️ **Gestão de Credenciais:**
> As credenciais de acesso e chaves de segurança não são versionadas neste repositório.
> Consulte o arquivo `src/main/java/com/umari/queueManager/config/securityConfig.java` ou o gerenciador de segredos do servidor de CI/CD para administração de usuários.

## 📡 Integração (API & WebSockets)

O sistema expõe interfaces para integração com periféricos (TVs, Totens, Impressoras).

### Endpoints REST (Internos)

* `POST /api/tickets`: Geração e impressão de senhas.
* `GET /api/tickets/info-totem`: Dados de telemetria para o totem (tempo estimado).
* `GET /api/tickets/dashboard`: Métricas consolidadas para gestão.

### Canais WebSocket

* `/topic/senhas`: *Broadcast* de eventos de fila (nova senha, chamada, cancelamento) para atualização passiva das interfaces.

## 🧹 Rotinas Automáticas

O sistema opera de forma autônoma com rotinas agendadas (`Cron Jobs`):

* **Reset Diário (00:00):** Arquivamento automático de atendimentos finalizados e reinicialização da sequência de senhas para o dia seguinte.

---

**© 2024 QueueManager Solutions.**
Todo o código fonte contido neste repositório é proprietário e confidencial. A cópia, distribuição ou engenharia reversa não autorizada é estritamente proibida.
