let stompClientAdmin = null;

document.addEventListener("DOMContentLoaded", function() {
    console.log("Painel Admin Iniciado");
    carregarDados();
    conectarWebSocketAdmin();
});

function conectarWebSocketAdmin() {
    const socket = new SockJS('/ws-queue');
    stompClientAdmin = Stomp.over(socket);
    stompClientAdmin.debug = null;
    stompClientAdmin.connect({}, function() {
        const subscription = stompClientAdmin.subscribe('/topic/senhas', function() {
            carregarDados();
        });
        window.addEventListener('beforeunload', function() {
            subscription.unsubscribe();
            stompClientAdmin.disconnect();
        });
    });
}

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
        let nomeMesa = "Mesa Desconhecida";
        if (t.atendente) {
            if (t.atendente.toLowerCase() === 'admin') nomeMesa = "Mesa Gerente";
            else if (t.atendente.toLowerCase().includes('guiche')) {
                nomeMesa = "Mesa " + t.atendente.replace(/\D/g, '');
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

// --- RELATÓRIO DO ADMIN ---
async function carregarRelatorio() {
    const painel = document.getElementById('painel-relatorio');
    const corpoTabela = document.getElementById('corpo-tabela-relatorio');

    painel.style.display = 'block';
    corpoTabela.innerHTML = '<tr><td colspan="5" class="text-center">Carregando dados...</td></tr>';

    try {
        const response = await fetch('/api/tickets/relatorio/mesas');

        if (!response.ok) {
            alert("Erro ao buscar relatório. Verifique se você está logado como Admin.");
            return;
        }

        const tickets = await response.json();
        corpoTabela.innerHTML = '';

        if (tickets.length === 0) {
            corpoTabela.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Nenhum atendimento finalizado hoje.</td></tr>';
            return;
        }

        tickets.forEach(ticket => {
            const dataHora = ticket.createdAt ? new Date(ticket.createdAt).toLocaleString('pt-BR') : '-';

            let nomeMesa = ticket.atendente || 'Desconhecido';
            if (nomeMesa.includes('guiche')) {
                nomeMesa = "Mesa " + nomeMesa.replace(/\D/g, '');
            }

            const row = `
                <tr>
                    <td class="fw-bold">${ticket.numero}</td>
                    <td>${ticket.nomeCliente || 'Não informado'}</td>
                    <td><span class="badge bg-secondary">${nomeMesa}</span></td>
                    <td>${ticket.tipoTicket}</td>
                    <td>${dataHora}</td>
                </tr>
            `;
            corpoTabela.innerHTML += row;
        });

    } catch (error) {
        console.error("Erro no relatório:", error);
        corpoTabela.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Erro ao carregar dados.</td></tr>';
    }
}

function baixarCsv() {
    window.location.href = '/api/tickets/historico/exportar';
}

function fazerLogout() {
    window.location.href = "/logout";
}

const API_USUARIOS = '/api/usuarios';
let modalUsuarios = null;

document.addEventListener("DOMContentLoaded", function() {
    document.getElementById('formUsuario').addEventListener('submit', function(e) {
        e.preventDefault();
        criarUsuario();
    });
});

function abrirGestaoUsuarios() {
    if (!modalUsuarios) {
        modalUsuarios = new bootstrap.Modal(document.getElementById('modalUsuarios'));
    }
    modalUsuarios.show();
    listarUsuarios();
}

async function listarUsuarios() {
    const tbody = document.getElementById('lista-usuarios');
    tbody.innerHTML = '<tr><td colspan="3" class="text-center">Carregando...</td></tr>';

    try {
        const response = await fetch(API_USUARIOS);
        if (response.ok) {
            const usuarios = await response.json();
            tbody.innerHTML = '';

            usuarios.forEach(u => {
                const badge = u.role === 'ADMIN' ? 'bg-danger' : 'bg-primary';
                const row = `
                    <tr>
                        <td class="fw-bold">${u.username}</td>
                        <td><span class="badge ${badge}">${u.role}</span></td>
                        <td class="text-success small">Ativo</td>
                    </tr>
                `;
                tbody.innerHTML += row;
            });
        }
    } catch (error) {
        console.error("Erro ao listar usuários:", error);
        tbody.innerHTML = '<tr><td colspan="3" class="text-danger">Erro ao carregar lista.</td></tr>';
    }
}

async function criarUsuario() {
    const loginInput = document.getElementById('novoLogin');
    const senhaInput = document.getElementById('novaSenha');
    const perfilInput = document.getElementById('novoPerfil');

    const payload = {
        username: loginInput.value.trim(),
        password: senhaInput.value,
        role: perfilInput.value
    };

    try {
        const response = await fetch(API_USUARIOS, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert("Usuário criado com sucesso!");
            loginInput.value = "";
            senhaInput.value = "";
            listarUsuarios();
        } else {
            const erro = await response.text();
            alert("Erro: " + erro);
        }
    } catch (error) {
        alert("Erro de conexão ao criar usuário.");
    }
}