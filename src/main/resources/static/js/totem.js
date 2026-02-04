const API_URL = '/api/tickets';

document.addEventListener('DOMContentLoaded', () => {
    atualizarInfoFila();
    setInterval(atualizarInfoFila, 30000);
});


async function atualizarInfoFila() {
    try {
        const response = await fetch(`${API_URL}/info-totem`);

        if (response.ok) {
            const dados = await response.json();

            const textoTitulo = dados.fila === 0
                ? "Atendimento Imediato"
                : `Pessoas na fila: ${dados.fila}`;

            let textoSub = "";
            if (dados.fila === 0) {
                textoSub = `Pode vir! Vagas hoje: ${dados.vagasRestantes}`;
            } else {
                textoSub = `Previsão de atendimento: ${dados.previsao}`;
            }

            document.getElementById('info-fila').innerText = textoTitulo;
            document.querySelector('#painel-fila small').innerText = textoSub;

             const painel = document.getElementById('painel-fila');
            if(dados.vagasRestantes < 5) {
                painel.className = "alert alert-danger mt-3 shadow-sm";
            } else {
                painel.className = "alert alert-info mt-3 shadow-sm";
            }
        }
    } catch (e) {
        console.error("Erro ao buscar fila", e);
    }
}
function formatarTempo(minutos) {
    if (minutos < 60) {
        return `${minutos} min`;
    }
    const horas = Math.floor(minutos / 60);
    const minRestantes = minutos % 60;
    if (minRestantes === 0) return `${horas}h`;
    return `${horas}h ${minRestantes}min`;
}

async function gerarSenha(tipo) {
    const nomeInput = document.getElementById('inputNome');
    const nome = nomeInput.value.trim();

    let url = `${API_URL}?tipoTicket=${tipo}`;
    if (nome) {
        url += `&nomeCliente=${encodeURIComponent(nome)}`;
    }

    try {
        const response = await fetch(url, { method: 'POST' });

        if (response.ok) {
            const ticket = await response.json();
            mostrarSenha(ticket);
            atualizarInfoFila();
        } else {
            const erroJson = await response.json().catch(() => null);
            if (erroJson && erroJson.mensagem) {
                alert("Atenção: " + erroJson.mensagem);
            } else {
                alert("Erro ao gerar senha. Tente novamente.");
            }
        }
    } catch (error) {
        alert("Erro de conexão com o servidor.");
        console.error(error);
    }
}

function mostrarSenha(ticket) {
    document.getElementById('inputNome').parentElement.style.display = 'none';
    document.querySelectorAll('.btn-ticket').forEach(btn => btn.style.display = 'none');
    document.getElementById('painel-fila').style.display = 'none';

    const resultDiv = document.getElementById('ticket-result');
    resultDiv.style.display = 'block';

    document.getElementById('numero-senha').innerText = ticket.numero;

    const nomeDisplay = document.getElementById('nome-exibido');
    if (ticket.nomeCliente) {
        nomeDisplay.innerText = "Olá, " + ticket.nomeCliente + "!";
    } else {
        nomeDisplay.innerText = "";
    }

    setTimeout(resetTela, 8000);
}

function resetTela() {
    document.getElementById('ticket-result').style.display = 'none';
    document.querySelectorAll('.btn-ticket').forEach(btn => btn.style.display = 'block');
    document.getElementById('inputNome').parentElement.style.display = 'block';

    document.getElementById('painel-fila').style.display = 'block';

    document.getElementById('inputNome').value = "";
    atualizarInfoFila();
}