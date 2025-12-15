const API_URL = 'http://localhost:8080/api/tickets';
let stompClient = null;
let ticketEmAtendimento = null; // Para guardar o ID do ticket atual

// Inicia tudo quando a página carrega
document.addEventListener('DOMContentLoaded', () => {
    carregarFila();
    conectarWebSocket();
});

// --- 1. FUNÇÕES DE API ---

async function carregarFila() {
    try {
        const response = await fetch(API_URL); // GET /api/tickets (O browser envia o cookie de login sozinho)
        if (response.ok) {
            const lista = await response.json();
            atualizarTabela(lista);
            descobrirAtual(lista); // Tenta achar se já tem alguém sendo atendido
        }
    } catch (error) {
        console.error("Erro ao carregar fila:", error);
    }
}

async function chamarProximo() {
    try {
        // Chama o endpoint inteligente que criámos (já finaliza o anterior e chama o próximo)
        const response = await fetch(`${API_URL}/proximo`, { method: 'POST' });

        if (response.ok) {
            const ticket = await response.json();
            atualizarPainelAtual(ticket);
            carregarFila(); // Atualiza a lista para remover quem foi chamado
        } else {
            const erro = await response.text();
            if (response.status === 500) {
                alert("A fila está vazia!");
            } else {
                alert("Erro: " + erro);
            }
        }
    } catch (error) {
        console.error("Erro ao chamar próximo:", error);
    }
}

async function finalizarAtual() {
    // 1. Se houver alguém na mesa, finaliza primeiro
    if (ticketEmAtendimento) {
        try {
            const url = `${API_URL}/${ticketEmAtendimento.id}/status?novoStatus=ATENDIDO`;
            await fetch(url, { method: 'PUT' });
        } catch (error) {
            console.error("Erro ao finalizar ticket individual:", error);
        }
    }

    // 2. AGORA FAZ O LOG: Chama o endpoint de Pausa que limpa tudo
    try {
        const response = await fetch(`${API_URL}/pausa`, { method: 'POST' });

        if (response.ok) {
            // Limpa a tela
            limparPainelAtual();
            // A lista vai atualizar sozinha (ficando vazia de atendidos)
            carregarFila();
            alert("Bom descanso! ☕\nAs senhas atendidas foram guardadas no histórico.");
        } else {
            alert("Erro ao realizar pausa.");
        }
    } catch (error) {
        console.error("Erro ao conectar:", error);
    }
}
// --- 2. ATUALIZAÇÃO DA TELA (DOM) ---

function atualizarTabela(lista) {
    const tbody = document.getElementById('lista-espera');
    tbody.innerHTML = '';

    // Filtra apenas os que estão AGUARDANDO (caso a API traga outros)
    const emEspera = lista.filter(t => t.status === 'AGUARDANDO');

    if (emEspera.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Ninguém na fila 🎉</td></tr>';
        return;
    }

    emEspera.forEach(t => {
        const tr = document.createElement('tr');
        // Formata a data/hora (pega só a hora)
        const hora = new Date(t.createdAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

        // Define classe de cor para o tipo
        const badgeClass = t.tipoTicket === 'PRIORITARIO' ? 'bg-warning text-dark' : 'bg-primary';

        tr.innerHTML = `
            <td class="fw-bold">${t.numero}</td>
            <td>${t.nomeCliente || '-'}</td>
            <td><span class="badge ${badgeClass}">${t.tipoTicket}</span></td>
            <td>${hora}</td>
            <td>
                <button onclick="cancelarTicket('${t.id}')" class="btn btn-sm btn-outline-danger" title="Cancelar">✕</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function atualizarPainelAtual(ticket) {
    ticketEmAtendimento = ticket;
    document.getElementById('atual-numero').innerText = ticket.numero;
    document.getElementById('atual-nome').innerText = ticket.nomeCliente || "Cliente sem nome";
    document.getElementById('atual-tipo').innerText = ticket.tipoTicket;

    // Muda a cor do badge dependendo do tipo
    const badge = document.getElementById('atual-tipo');
    badge.className = ticket.tipoTicket === 'PRIORITARIO' ? 'badge bg-warning text-dark' : 'badge bg-primary';
}

function limparPainelAtual() {
    ticketEmAtendimento = null;
    document.getElementById('atual-numero').innerText = "---";
    document.getElementById('atual-nome').innerText = "Pausa / Livre";
    document.getElementById('atual-tipo').innerText = "-";
    document.getElementById('atual-tipo').className = "badge bg-secondary";
}

// Função auxiliar para descobrir se a página foi recarregada e já havia alguém sendo atendido
// (Isso exigiria um endpoint novo na API "getCurrentTicket", mas podemos ignorar por agora)
function descobrirAtual(lista) {
    // Por enquanto, o endpoint /api/tickets só retorna AGUARDANDO.
    // Se quiséssemos persistir o "Em Atendimento" na tela após F5, precisaríamos mudar o GET da API.
    // Para simplificar, assumimos que começa vazio.
}
async function criarTicketAvulso() {
    const nomeInput = document.getElementById('nomeAvulso');
    const nome = nomeInput.value.trim();

    if (!nome) {
        alert("Por favor, digite um nome.");
        return;
    }

    try {
        // Envia POST com tipo AVULSO e o nome digitado
        const url = `${API_URL}?tipoTicket=AVULSO&nomeCliente=${encodeURIComponent(nome)}`;

        const response = await fetch(url, { method: 'POST' });

        if (response.ok) {
            // Fecha o modal
            const modalElement = document.getElementById('modalAvulso');
            const modal = bootstrap.Modal.getInstance(modalElement);
            modal.hide();

            // Limpa o campo
            nomeInput.value = "";

            // A lista vai atualizar sozinha pelo WebSocket, mas forçamos aqui para garantir
            carregarFila();

            // Feedback opcional
            // alert("Senha Avulsa Criada com Sucesso!");
        } else {
            alert("Erro ao criar senha avulsa.");
        }
    } catch (error) {
        console.error("Erro:", error);
        alert("Erro de conexão.");
    }
}

// --- 3. WEBSOCKET (TEMPO REAL) ---

function conectarWebSocket() {
    const socket = new SockJS('/ws-queue');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Desativa logs no console para ficar limpo

    stompClient.connect({}, function (frame) {
        console.log('Conectado ao WebSocket!');

        // Inscreve-se no canal para receber atualizações
        stompClient.subscribe('/topic/senhas', function (mensagem) {
            // Sempre que chegar uma mensagem, recarrega a fila
            // (Podíamos ser mais cirúrgicos e adicionar só a linha, mas recarregar tudo é mais seguro e fácil)
            carregarFila();
        });
    });
}

async function abrirHistorico() {
    try {
        const response = await fetch(`${API_URL}/historico`);
        if (response.ok) {
            const lista = await response.json();
            preencherTabelaHistorico(lista);

            // Abre o modal usando Bootstrap
            const modal = new bootstrap.Modal(document.getElementById('modalHistorico'));
            modal.show();
        }
    } catch (error) {
        console.error("Erro ao carregar histórico:", error);
        alert("Erro ao carregar histórico.");
    }
}

function preencherTabelaHistorico(lista) {
    const tbody = document.getElementById('tabela-historico');
    tbody.innerHTML = '';

    // Ordena do mais recente para o mais antigo
    lista.sort((a, b) => new Date(b.dataArquivamento) - new Date(a.dataArquivamento));

    if (lista.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">Histórico vazio.</td></tr>';
        return;
    }

    lista.forEach(t => {
        const tr = document.createElement('tr');
        // Formata datas
        const dataCriacao = new Date(t.dataCriacao).toLocaleString('pt-BR');
        const dataFim = new Date(t.dataArquivamento).toLocaleString('pt-BR');

        tr.innerHTML = `
            <td class="fw-bold">${t.numero}</td>
            <td>${t.nomeCliente || '-'}</td>
            <td><span class="badge bg-secondary">${t.tipo}</span></td>
            <td class="small">${dataCriacao}</td>
            <td class="small fw-bold">${dataFim}</td>
        `;
        tbody.appendChild(tr);
    });
}
async function cancelarTicket(id) {
    if (!confirm("Tem a certeza que deseja cancelar esta senha?")) {
        return;
    }

    try {
        // Usa a rota que já existe no teu TicketController: PUT /api/tickets/{id}/status
        const response = await fetch(`${API_URL}/${id}/status?novoStatus=CANCELADO`, {
            method: 'PUT'
        });

        if (response.ok) {
            // Atualiza a tabela para remover o item cancelado
            carregarFila();
        } else {
            alert("Erro ao cancelar o ticket.");
        }
    } catch (error) {
        console.error("Erro:", error);
        alert("Erro de conexão ao tentar cancelar.");
    }
}