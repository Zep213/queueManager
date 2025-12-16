// Exemplo de como você atualizaria o contador dinamicamente
document.addEventListener("DOMContentLoaded", function() {
    console.log("Painel de Monitoramento Iniciado");

    // Exemplo: Função para atualizar o tempo de atendimento (apenas visual)
    setInterval(() => {
        // Aqui você faria uma requisição ao backend para pegar os dados reais
        // fetch('/api/fila/status')...
    }, 5000);
});