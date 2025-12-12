const API_URL = 'http://localhost:8080/api/tickets';

async function gerarSenha(tipo) {
    // 1. Pega o valor do input de nome
    const nomeInput = document.getElementById('inputNome');
    const nome = nomeInput.value.trim();

    // 2. Monta a URL com o nome (se existir)
    // Ex: /api/tickets?tipoTicket=NORMAL&nomeCliente=Joao
    let url = `${API_URL}?tipoTicket=${tipo}`;
    if (nome) {
        url += `&nomeCliente=${encodeURIComponent(nome)}`;
    }

    try {
        const response = await fetch(url, { method: 'POST' });

        if (!response.ok) {
            throw new Error('Erro ao gerar senha');
        }

        const ticket = await response.json();

        // Mostra na tela
        mostrarSenha(ticket);

    } catch (error) {
        alert("Erro ao conectar com o servidor.");
        console.error(error);
    }
}

function mostrarSenha(ticket) {
    // Esconde form, mostra senha
    document.getElementById('inputNome').parentElement.style.display = 'none';
    document.querySelectorAll('.btn-ticket').forEach(btn => btn.style.display = 'none');

    const resultDiv = document.getElementById('ticket-result');
    resultDiv.style.display = 'block';

    document.getElementById('numero-senha').innerText = ticket.numero;

    // Mostra o nome se tiver
    const nomeDisplay = document.getElementById('nome-exibido');
    if (ticket.nomeCliente) {
        nomeDisplay.innerText = "Olá, " + ticket.nomeCliente + "!";
    } else {
        nomeDisplay.innerText = "";
    }

    // Reseta após 8 segundos
    setTimeout(resetTela, 8000);
}

function resetTela() {
    // Volta tudo ao normal
    document.getElementById('ticket-result').style.display = 'none';
    document.querySelectorAll('.btn-ticket').forEach(btn => btn.style.display = 'block');
    document.getElementById('inputNome').parentElement.style.display = 'block';

    // Limpa o campo de nome para o próximo
    document.getElementById('inputNome').value = "";
}