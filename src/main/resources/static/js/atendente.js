const API_URL = '/api/tickets';
let stompClient = null;
let ticketEmAtendimento = null;

// Inicia ao carregar a página
document.addEventListener('DOMContentLoaded', () => {
    carregarFila();
    conectarWebSocket();
});

// --- FUNÇÕES PRINCIPAIS ---

async function carregarFila() {
    try {
        const response = await fetch(API_URL);
        if (response.ok) {
            const lista = await response.json();
            atualizarTabela(lista);
        }
    } catch (error) {
        console.error("Erro ao carregar fila:", error);
    }
}

async function chamarProximo() {
    try {
        if (ticketEmAtendimento) {
            await finalizarSemPausa();
        }

        const response = await fetch(`${API_URL}/proximo`, { method: 'POST' });

        if (response.ok) {
            const ticket = await response.json();
            if(ticket) {
                atualizarPainelAtual(ticket);
                carregarFila();
            }
        } else {
            alert("A fila está vazia ou não há senhas pendentes!");
        }
    } catch (error) {
        console.error("Erro ao chamar próximo:", error);
    }
}

async function finalizarAtual() {
    if (!ticketEmAtendimento) return;

    try {
        await finalizarSemPausa();
        limparPainelAtual();
        carregarFila();
    } catch (error) {
        alert("Erro ao finalizar atendimento.");
    }
}

async function finalizarSemPausa() {
    if (ticketEmAtendimento) {
        const url = `${API_URL}/${ticketEmAtendimento.id}/status?novoStatus=ATENDIDO`;
        await fetch(url, { method: 'PUT' });
        ticketEmAtendimento = null;
    }
}

async function cancelarTicket(id) {
    if (!confirm("Tem a certeza que deseja cancelar esta senha?")) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${id}/status?novoStatus=CANCELADO`, {
            method: 'PUT'
        });

        if (response.ok) {
            carregarFila();
        } else {
            alert("Erro ao cancelar ticket.");
        }
    } catch (error) {
        console.error("Erro ao cancelar:", error);
        alert("Erro de conexão.");
    }
}

async function criarTicketAvulso() {
    const nomeInput = document.getElementById('nomeAvulso');
    const nome = nomeInput.value.trim();

    if (!nome) {
        alert("Digite um nome.");
        return;
    }

    try {
        const url = `${API_URL}?tipoTicket=AVULSO&nomeCliente=${encodeURIComponent(nome)}`;
        const response = await fetch(url, { method: 'POST' });

        if (response.ok) {
            const modalEl = document.getElementById('modalAvulso');
            const modal = bootstrap.Modal.getInstance(modalEl);
            modal.hide();
            nomeInput.value = "";
            carregarFila();
        } else {
            alert("Erro ao criar senha.");
        }
    } catch (error) {
        console.error("Erro:", error);
    }
}

// --- ATUALIZAÇÃO VISUAL ---

function atualizarTabela(lista) {
    const tbody = document.getElementById('lista-espera');
    tbody.innerHTML = '';

    const emEspera = lista.filter(t => t.status === 'AGUARDANDO');

    if (emEspera.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">Ninguém na fila 🎉</td></tr>';
        return;
    }

    emEspera.forEach(t => {
        const tr = document.createElement('tr');
        const hora = new Date(t.createdAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
        const badgeClass = t.tipoTicket === 'PRIORITARIO' ? 'bg-warning text-dark' : 'bg-primary';

        tr.innerHTML = `
            <td class="ps-3 fw-bold">${t.numero}</td>
            <td>${t.nomeCliente || '-'}</td>
            <td><span class="badge ${badgeClass}">${t.tipoTicket}</span></td>
            <td>${hora}</td>
            <td class="text-end pe-3">
                <button onclick="cancelarTicket('${t.id}')" class="btn btn-sm btn-outline-danger" title="Cancelar">Cancelar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function atualizarPainelAtual(ticket) {
    ticketEmAtendimento = ticket;
    document.getElementById('atual-numero').innerText = ticket.numero;
    document.getElementById('atual-nome').innerText = ticket.nomeCliente || "Cliente sem nome";

    const badge = document.getElementById('atual-tipo');
    badge.innerText = ticket.tipoTicket;
    badge.className = ticket.tipoTicket === 'PRIORITARIO' ? 'badge bg-warning text-dark fs-5' : 'badge bg-primary fs-5';
}

function limparPainelAtual() {
    ticketEmAtendimento = null;
    document.getElementById('atual-numero').innerText = "---";
    document.getElementById('atual-nome').innerText = "Livre / Aguardando";
    const badge = document.getElementById('atual-tipo');
    badge.innerText = "-";
    badge.className = "badge bg-secondary fs-5";
}

// --- HISTÓRICO E WEBSOCKET ---

async function abrirHistorico() {
    try {
        const response = await fetch(`${API_URL}/historico`);
        if (response.ok) {
            const lista = await response.json();
            preencherTabelaHistorico(lista);
            new bootstrap.Modal(document.getElementById('modalHistorico')).show();
        }
    } catch (e) {
        console.error(e);
        alert("Erro ao carregar histórico.");
    }
}

function preencherTabelaHistorico(lista) {
    const tbody = document.getElementById('tabela-historico');
    tbody.innerHTML = '';

    lista.sort((a,b) => new Date(b.dataArquivamento) - new Date(a.dataArquivamento));

    if (lista.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Histórico vazio.</td></tr>';
        return;
    }

    lista.forEach(t => {
        // Formata o nome da mesa para exibição
        let mesa = t.atendente || 'N/A';
        if (mesa.includes('guiche')) mesa = 'Mesa ' + mesa.replace(/\D/g, '');

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${t.numero}</td>
            <td>${t.nomeCliente || '-'}</td>
            <td>${t.tipo}</td>
            <td>${mesa}</td>
            <td>${new Date(t.dataArquivamento).toLocaleTimeString()}</td>
        `;
        tbody.appendChild(tr);
    });
}

// FUNÇÕES NOVAS PARA O BOTÃO DO HISTÓRICO
function baixarHistoricoCsv() {
    window.location.href = `${API_URL}/historico/exportar`;
}

function conectarWebSocket() {
    const socket = new SockJS('/ws-queue');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/senhas', function () {
            carregarFila();
        });
    });
}

async function realizarPausa() {
    if (!confirm("Deseja arquivar os atendimentos finalizados e fazer uma pausa?")) return;

    try {
        const response = await fetch(`${API_URL}/pausa`, { method: 'POST' });
        if (response.ok) {
            alert("Senhas arquivadas! Bom descanso.");
            limparPainelAtual();
            carregarFila();
        }
    } catch (e) {
        console.error(e);
    }
}