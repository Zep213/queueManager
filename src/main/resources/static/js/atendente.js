const API_URL = '/api/tickets';
let stompClient = null;
let ticketEmAtendimento = null; // Variável Global Importante

// Inicia ao carregar a página
document.addEventListener('DOMContentLoaded', () => {
    console.log("Iniciando Atendente...");
    carregarFila();
    conectarWebSocket();
});

// --- FUNÇÕES PRINCIPAIS ---

async function carregarFila() {
    try {
        const response = await fetch(API_URL);
        if (response.ok) {
            const lista = await response.json();

            // 1. Procura se ALGUÉM (qualquer um) está com status EM_ATENDIMENTO
            const atendendoAgora = lista.find(t => t.status === 'EM_ATENDIMENTO');

            if (atendendoAgora) {
                console.log("Retomando atendimento de:", atendendoAgora.numero);
                atualizarPainelAtual(atendendoAgora);
            } else {
                limparPainelAtual();
            }

            // 2. Atualiza a lista de espera (Quem está AGUARDANDO)
            atualizarTabela(lista);
        } else {
            console.error("Erro ao buscar fila. Status:", response.status);
        }
    } catch (error) {
        console.error("Erro de conexão ao carregar fila:", error);
    }
}

async function chamarProximo() {
    if (ticketEmAtendimento) {
        if(!confirm(`Já existe um atendimento (${ticketEmAtendimento.numero}) em andamento. Finalizar e chamar o próximo?`)) return;

        const sucesso = await finalizarSemPausa();
        if (!sucesso) return; // Se der erro ao finalizar, para tudo
    }

    try {
        const response = await fetch(`${API_URL}/proximo`, { method: 'POST' });
        if (response.ok) {
            // O WebSocket vai atualizar a tela, mas forçamos aqui para ser rápido
            setTimeout(carregarFila, 200);
        } else {
            const erro = await response.json().catch(() => ({}));
            alert(erro.mensagem || "Não há ninguém na fila!");
        }
    } catch (error) {
        console.error("Erro ao chamar próximo:", error);
        alert("Erro de conexão.");
    }
}

async function chamarEspecifico(id) {
    if (ticketEmAtendimento) {
        if(!confirm(`Finalizar atendimento atual (${ticketEmAtendimento.numero}) e chamar este cliente?`)) return;

        const sucesso = await finalizarSemPausa();
        if (!sucesso) return;
    } else {
        if(!confirm("Atender este cliente agora?")) return;
    }

    try {
        const response = await fetch(`${API_URL}/${id}/chamar`, { method: 'POST' });
        if (response.ok) {
            setTimeout(carregarFila, 200);
        } else {
            alert("Erro ao chamar o cliente. Talvez ele já tenha sido atendido.");
            carregarFila(); // Atualiza para ver se sumiu
        }
    } catch (error) {
        console.error("Erro:", error);
    }
}

// --- AQUI ESTÁ A CORREÇÃO DO FINALIZAR ---
async function finalizarAtual() {
    // 1. Verificação de Segurança
    if (!ticketEmAtendimento) {
        alert("O sistema não identificou nenhum atendimento em andamento para finalizar.");
        carregarFila(); // Tenta corrigir o estado
        return;
    }

    if (!confirm(`Finalizar o atendimento da senha ${ticketEmAtendimento.numero}?`)) return;

    // 2. Tenta Finalizar
    const sucesso = await finalizarSemPausa();

    // 3. Se deu certo, limpa a tela
    if (sucesso) {
        limparPainelAtual();
        carregarFila();
    }
}

// Função auxiliar robusta
async function finalizarSemPausa() {
    if (!ticketEmAtendimento || !ticketEmAtendimento.id) {
        console.error("Erro: ID do ticket inválido:", ticketEmAtendimento);
        return false;
    }

    try {
        const url = `${API_URL}/${ticketEmAtendimento.id}/status?novoStatus=ATENDIDO`;
        console.log("Finalizando ticket ID:", ticketEmAtendimento.id);

        const response = await fetch(url, { method: 'PUT' });

        if (response.ok) {
            ticketEmAtendimento = null; // Limpa a variável global
            return true;
        } else {
            // Se o servidor recusar, mostra o erro
            const textoErro = await response.text();
            console.error("Erro servidor:", textoErro);
            alert("Erro ao finalizar: " + response.status);
            return false;
        }
    } catch (error) {
        console.error("Erro ao finalizar:", error);
        alert("Erro de conexão ao tentar finalizar.");
        return false;
    }
}

async function cancelarTicket(id) {
    if (!confirm("Tem a certeza que deseja cancelar esta senha?")) return;

    try {
        await fetch(`${API_URL}/${id}/status?novoStatus=CANCELADO`, { method: 'PUT' });
        carregarFila();
    } catch (error) {
        console.error("Erro ao cancelar:", error);
    }
}

async function criarTicketAvulso() {
    const nome = document.getElementById('nomeAvulso').value.trim();
    if (!nome) return alert("Digite um nome.");

    try {
        const url = `${API_URL}?tipoTicket=AVULSO&nomeCliente=${encodeURIComponent(nome)}`;
        const response = await fetch(url, { method: 'POST' });

        if (response.ok) {
            bootstrap.Modal.getInstance(document.getElementById('modalAvulso')).hide();
            document.getElementById('nomeAvulso').value = "";
            carregarFila();
        }
    } catch (error) {
        console.error(error);
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
                <button onclick="chamarEspecifico('${t.id}')" class="btn btn-sm btn-success me-1 text-white fw-bold">Atender</button>
                <button onclick="cancelarTicket('${t.id}')" class="btn btn-sm btn-outline-danger" title="Cancelar">Cancelar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function atualizarPainelAtual(ticket) {
    // AQUI É O PULO DO GATO: Atualizamos a variável global
    ticketEmAtendimento = ticket;

    document.getElementById('atual-numero').innerText = ticket.numero;
    document.getElementById('atual-nome').innerText = ticket.nomeCliente || "Cliente";

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

// --- WEBSOCKET ---
function conectarWebSocket() {
    const socket = new SockJS('/ws-queue');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/fila', function () {
            carregarFila();
        });
    });
}

// Histórico e Pausa...
async function abrirHistorico() {
    const response = await fetch(`${API_URL}/historico`);
    if(response.ok) {
        preencherTabelaHistorico(await response.json());
        new bootstrap.Modal(document.getElementById('modalHistorico')).show();
    }
}

function preencherTabelaHistorico(lista) {
    const tbody = document.getElementById('tabela-historico');
    tbody.innerHTML = '';
    lista.sort((a,b) => new Date(b.dataArquivamento) - new Date(a.dataArquivamento));

    lista.forEach(t => {
        let mesa = t.atendente || 'N/A';
        if (mesa.toLowerCase().includes('guiche')) mesa = 'Mesa ' + mesa.replace(/\D/g, '');

        tbody.innerHTML += `
            <tr>
                <td>${t.numero}</td>
                <td>${t.nomeCliente || '-'}</td>
                <td>${t.tipo}</td>
                <td>${mesa}</td>
                <td>${new Date(t.dataArquivamento).toLocaleTimeString()}</td>
            </tr>`;
    });
}

function baixarHistoricoCsv() { window.location.href = `${API_URL}/historico/exportar`; }

async function realizarPausa() {
    if (!confirm("Arquivar atendimentos e pausar?")) return;
    await fetch(`${API_URL}/pausa`, { method: 'POST' });
    limparPainelAtual();
    carregarFila();
}