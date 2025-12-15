const API_URL = 'http://localhost:8080/api/tickets';

// Ao carregar a página, atualiza a informação da fila
document.addEventListener('DOMContentLoaded', () => {
    atualizarInfoFila();
    // Atualiza a cada 30 segundos para manter o tempo estimado correto
    setInterval(atualizarInfoFila, 30000);
});

async function atualizarInfoFila() {
    try {
        // Precisamos criar este endpoint no TicketController (ver passo abaixo)
        // Se ainda não criou, vai dar erro 404, mas não quebra a tela.
        const response = await fetch(`${API_URL}/fila/tamanho`);

        if (response.ok) {
            const qtd = await response.json();
            const tempoEstimado = qtd * 10; // Regra de 10 min por pessoa

            const textoTitulo = qtd === 0
                ? "Fila vazia! Atendimento imediato."
                : `Pessoas na fila: ${qtd}`;

            const textoSub = qtd === 0
                ? "Retire sua senha agora."
                : `Tempo estimado de espera: ${tempoEstimado} minutos.`;

            document.getElementById('info-fila').innerText = textoTitulo;
            document.querySelector('#painel-fila small').innerText = textoSub;

            // Muda a cor do alerta se estiver cheio
            const painel = document.getElementById('painel-fila');
            if(qtd > 10) {
                painel.className = "alert alert-warning mt-3 shadow-sm";
            } else {
                painel.className = "alert alert-info mt-3 shadow-sm";
            }
        }
    } catch (e) {
        console.error("Erro ao buscar fila", e);
    }
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