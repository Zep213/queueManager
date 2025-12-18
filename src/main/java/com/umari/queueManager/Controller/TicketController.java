package com.umari.queueManager.Controller;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Enums.EnumTipoTicket;
import com.umari.queueManager.Model.TicketHistorico;
import com.umari.queueManager.service.TicketService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public Ticket gerarSenha(@RequestParam(defaultValue = "NORMAL") EnumTipoTicket tipoTicket,
                             @RequestParam String nomeCliente) {
        return ticketService.criarSenha(tipoTicket, nomeCliente);
    }

    @GetMapping
    public List<Ticket> listarFila() {
        return ticketService.listarSenhasEmEspera();
    }

    @PutMapping("/{id}/status")
    public Ticket atualizaStatus(
            @PathVariable String id,
            @RequestParam EnumTickets novoStatus,
            Authentication authentication) {
        String nomeAtendente = (authentication != null) ? authentication.getName() : "Desconhecido";
        return ticketService.atualizaStatusTicket(id, novoStatus, nomeAtendente);
    }

    @PostMapping("/proximo")
    public Ticket chamarProximoDaFila(Authentication authentication) {
        String nomeAtendente = (authentication != null) ? authentication.getName() : "Desconhecido";
        return ticketService.chamarProximo(nomeAtendente);
    }

    @PostMapping("/pausa")
    public void pausaGeral() {
        ticketService.realizarPausaEArquivar();
    }

    @GetMapping("/historico")
    public List<TicketHistorico> listarHistorico() {
        return ticketService.listarHistorico();
    }

    // --- CSV COM NOME DA MESA FORMATADO ---
    @GetMapping("/historico/exportar")
    public ResponseEntity<String> exportarHistoricoCsv() {
        List<TicketHistorico> historico = ticketService.listarHistorico();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        StringBuilder csv = new StringBuilder();
        // Cabeçalho
        csv.append("ID;Numero;Cliente;Tipo;Mesa/Atendente;Data Chegada;Data Atendimento\n");

        for (TicketHistorico t : historico) {
            String nomeMesa = "Desconhecido";
            if (t.getAtendente() != null) {
                if (t.getAtendente().toLowerCase().contains("guiche")) {
                    nomeMesa = "Mesa " + t.getAtendente().replaceAll("\\D", "");
                } else {
                    nomeMesa = t.getAtendente();
                }
            }

            csv.append(t.getId()).append(";")
                    .append(t.getNumero()).append(";")
                    .append(t.getNomeCliente() != null ? t.getNomeCliente() : "Sem Nome").append(";")
                    .append(t.getTipo()).append(";")
                    .append(nomeMesa).append(";") // Adiciona a mesa formatada
                    .append(t.getDataCriacao().format(formatter)).append(";")
                    .append(t.getDataArquivamento().format(formatter)).append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historico_atendimentos.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.toString());
    }

    @GetMapping("/relatorio/mesas")
    public ResponseEntity<List<Ticket>> getRelatorioMesas() {
        return ResponseEntity.ok(ticketService.gerarRelatorioAtendimentos());
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarAtendimento(@PathVariable String  id, String nomeAtendente) {
        ticketService.atualizaStatusTicket(id, EnumTickets.CANCELADO, nomeAtendente);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fila/tamanho")
    public ResponseEntity<Long> contarFila() {
        long qtd = ticketService.listarSenhasEmEspera().size();
        return ResponseEntity.ok(qtd);
    }

    @GetMapping("/info-totem")
    public ResponseEntity<Map<String, Object>> getInfoTotem() {
        int pessoasNaFila = ticketService.listarSenhasEmEspera().size();

        String previsaoTexto = ticketService.calcularPrevisaoAtendimento(pessoasNaFila);
        int limiteDiario = 15;
        long senhasHoje = ticketService.contarSenhasHoje();
        long vagasRestantes = limiteDiario - senhasHoje;

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("fila", pessoasNaFila);
        resposta.put("previsao", previsaoTexto); // <--- Campo Novo
        resposta.put("vagasRestantes", Math.max(0, vagasRestantes));

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        return ResponseEntity.ok(ticketService.getDadosDashboard());
    }


}