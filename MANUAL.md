# 📖 Manual de Uso - QueueManager

Este guia explica como operar as telas do sistema de filas.

## 1. Totem de Autoatendimento
**Acesso:** `http://localhost:8080/`

Esta é a tela que fica virada para o público na entrada.
* **Digitar Nome:** Opcional, mas recomendado para identificação.
* **Botão "Normal":** Gera uma senha com prefixo **N** (Ex: N001).
* **Botão "Prioritário":** Gera uma senha com prefixo **P** (Ex: P001). As senhas prioritárias são chamadas primeiro.
* **Informações:** O totem mostra quantas pessoas estão na espera e o tempo estimado.

---

## 2. Mesa de Atendimento (Funcionários)
**Acesso:** `http://localhost:8080/atendente.html`
*(Necessário Login: use `guiche01` / `user123`)*

Onde o atendente trabalha.
* **📢 Chamar Próximo:** O sistema busca automaticamente a próxima pessoa.
    * Ordem de prioridade: `Prioritário` -> `Normal` -> `Avulso`.
* **✅ Finalizar:** Conclui o atendimento atual e libera a mesa.
* **➕ Novo Avulso:** Permite criar uma senha manualmente (caso o cliente não tenha passado pelo totem).
* **📜 Histórico:** Mostra as senhas já atendidas.
* **Cancelar:** Na lista de espera, é possível cancelar uma senha caso a pessoa tenha desistido.

---

## 3. Painel do Gerente (Administrador)
**Acesso:** `http://localhost:8080/admin.html`
*(Necessário Login: use `admin` / `admin123`)*

Tela de monitoramento em tempo real.
* **Lado Esquerdo (Fila):** Lista completa de quem está aguardando. Senhas prioritárias aparecem destacadas em amarelo.
* **Lado Direito (Mesas):** Mostra **quem** está atendendo **quem**.
    * Se a mesa estiver livre, não aparece (ou aparece como inativa).
    * Assim que um atendente clica em "Chamar", o card aparece aqui instantaneamente.
    * O nome da mesa é identificado automaticamente (Ex: login `guiche01` vira "Mesa 01").

---

## 4. Dúvidas Comuns

**P: A fila não atualizou na minha tela.**
R: O sistema usa atualização automática. Se travar, pressione `F5` para recarregar.

**P: Como zero a fila para o dia seguinte?**
R: O sistema faz isso automaticamente à meia-noite. Se precisar forçar, o atendente pode clicar no botão "☕ Pausar" e confirmar o arquivamento.