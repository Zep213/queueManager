document.addEventListener("DOMContentLoaded", function() {
    console.log("Painel Admin Iniciado");
    carregarDados();
    // Atualiza a cada 2 segundos
    setInterval(carregarDados, 2000);
});

async function carregarDados() {
    try {
        const response = await fetch('/api/tickets/dashboard');
        if (response.ok) {
            const dados = await response.json();
            atualizarFila(dados.filaGeral);
            atualizarMesas(dados.mesasAtivas);
        }
    } catch (error) {
        console.error("Erro ao buscar dados:", error);
    }
}

// --- COLUNA DA ESQUERDA: FILA DE ESPERA ---
function atualizarFila(lista) {
    const container = document.getElementById("lista-espera-container");
    const contador = document.getElementById("contador-fila");

    contador.innerText = lista.length;
    container.innerHTML = "";

    if (lista.length === 0) {
        container.innerHTML = '<li class="list-group-item text-center text-muted">Fila vazia.</li>';
        return;
    }

    lista.forEach(t => {
        // Define cor se for Prioritário
        const bg = t.tipoTicket === 'PRIORITARIO' ? 'bg-warning-subtle' : '';
        const badge = t.tipoTicket === 'PRIORITARIO' ? 'bg-warning text-dark' : 'bg-primary';

        const item = `
            <li class="list-group-item d-flex justify-content-between align-items-center ${bg}">
                <div>
                    <span class="fw-bold fs-5">${t.numero}</span>
                    <div class="small text-muted">${t.nomeCliente || 'Cliente'}</div>
                </div>
                <span class="badge ${badge}">${t.tipoTicket}</span>
            </li>
        `;
        container.innerHTML += item;
    });
}

// --- COLUNA DA DIREITA: MESAS ATENDENDO ---
function atualizarMesas(lista) {
    const container = document.getElementById("mesas-container");
    container.innerHTML = "";

    if (!lista || lista.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center text-muted mt-5">
                <h4>Nenhum atendimento agora.</h4>
                <p>As mesas aparecerão aqui quando chamarem alguém.</p>
            </div>`;
        return;
    }

    lista.forEach(t => {
        // Formata o nome da mesa (ex: guiche01 -> Mesa 01)
        let nomeMesa = "Mesa Desconhecida";
        if (t.atendente) {
            if (t.atendente.toLowerCase() === 'admin') nomeMesa = "Mesa Gerente";
            else if (t.atendente.toLowerCase().includes('guiche')) {
                nomeMesa = "Mesa " + t.atendente.replace(/\D/g, ''); // Pega só os números
            } else {
                nomeMesa = t.atendente.toUpperCase();
            }
        }

        const corBadge = t.tipoTicket === 'PRIORITARIO' ? 'bg-warning text-dark' : 'bg-primary';

        const card = `
        <div class="col-md-6 col-lg-4">
            <div class="card border-primary h-100 shadow-sm status-card occupied">
                <div class="card-body text-center">
                    <h5 class="card-title text-secondary mb-3">${nomeMesa}</h5>

                    <div class="p-3 bg-light rounded border">
                        <small class="text-uppercase text-muted">Atendendo</small>
                        <h1 class="fw-bold text-dark my-1">${t.numero}</h1>
                        <div class="text-truncate fw-bold text-primary">${t.nomeCliente || ''}</div>
                        <span class="badge ${corBadge} mt-2">${t.tipoTicket}</span>
                    </div>

                    <div class="mt-3">
                        <span class="badge bg-success blink">Em Andamento</span>
                    </div>
                </div>
            </div>
        </div>`;

        container.innerHTML += card;
    });
}