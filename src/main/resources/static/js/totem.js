const API_URL = '/api/tickets';

// Ao carregar a página, atualiza a informação da fila
document.addEventListener('DOMContentLoaded', () => {
    atualizarInfoFila();
    // Atualiza a cada 30 segundos para manter o tempo estimado correto
    setInterval(atualizarInfoFila, 30000);
});


async function atualizarInfoFila() {
    try {
        const response = await fetch(`${API_URL}/info-totem`);

        if (response.ok) {
            const dados = await response.json();

            // 1. Título
            const textoTitulo = dados.fila === 0
                ? "Atendimento Imediato"
                : `Pessoas na fila: ${dados.fila}`;

            // 2. Subtítulo com a NOVA Previsão
            let textoSub = "";
            if (dados.fila === 0) {
                textoSub = `Pode vir! Vagas hoje: ${dados.vagasRestantes}`;
            } else {
                // Aqui usamos o texto que vem do Java (Ex: Hoje às 14:30)
                textoSub = `Previsão de atendimento: ${dados.previsao}`;
            }

            document.getElementById('info-fila').innerText = textoTitulo;
            document.querySelector('#painel-fila small').innerText = textoSub;

            // ... (o resto do código de cores mantém igual) ...
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
// Função para converter minutos em Horas e Minutos
function formatarTempo(minutos) {
    if (minutos < 60) {
        return `${minutos} min`;
    }
    const horas = Math.floor(minutos / 60);
    const minRestantes = minutos % 60;
    // Se minuto for 0, não mostra (ex: "2h" em vez de "2h 00min")
    if (minRestantes === 0) return `${horas}h`;
    return `${horas}h ${minRestantes}min`;
}

async function gerarSenha(tipo) {
    const nomeInput = document.getElementById('inputNome');
    const nome = nomeInput.value.trim();

    // Constrói a URL
    let url = `${API_URL}?tipoTicket=${tipo}`;
    if (nome) {
        url += `&nomeCliente=${encodeURIComponent(nome)}`;
    }

    try {
        const response = await fetch(url, { method: 'POST' });

        if (response.ok) {
            const ticket = await response.json();
            mostrarSenha(ticket);
            // Atualiza a fila imediatamente após gerar
            atualizarInfoFila();
        } else {
            // Tratamento de erro melhorado (Lê o JSON do backend)
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
    // Esconde form e info da fila
    document.getElementById('inputNome').parentElement.style.display = 'none';
    document.querySelectorAll('.btn-ticket').forEach(btn => btn.style.display = 'none');
    document.getElementById('painel-fila').style.display = 'none'; // Esconde a info da fila

    // Mostra resultado
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

    // Mostra a info da fila novamente
    document.getElementById('painel-fila').style.display = 'block';

    document.getElementById('inputNome').value = "";
    atualizarInfoFila(); // Atualiza dados
}