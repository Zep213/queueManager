# QueueManager

**Versão:** 1.0.0
**Status:** Produção

Sistema corporativo de gestão de filas e atendimento presencial com processamento em tempo real. Projetado para alta disponibilidade e previsibilidade de fluxo.

---

## 📋 Especificações Técnicas

O projeto segue uma arquitetura baseada em microsserviços e comunicação assíncrona.

* **Core:** Java 21 (LTS) / Spring Boot 3.2
* **Database:** MongoDB (NoSQL)
* **Real-time:** WebSocket (STOMP protocol)
* **Frontend:** Thymeleaf + Bootstrap 5
* **Security:** Spring Security (RBAC)

## ⚙️ Requisitos de Ambiente

Para execução em ambiente de produção ou homologação:

1.  **JDK 21** ou superior.
2.  **MongoDB** (v5.0+) operando na porta `27017` (ou configurado via variáveis de ambiente).
3.  **Maven** (para build).

## 🚀 Instalação e Build

O projeto é privado e proprietário. Certifique-se de ter as chaves SSH configuradas para acesso ao repositório.

**1. Clonagem e Build**
```bash
git clone git@github.com:sua-empresa/queuemanager.git
./mvnw clean package -DskipTests
