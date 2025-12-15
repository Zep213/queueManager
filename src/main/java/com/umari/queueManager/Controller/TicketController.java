package com.umari.queueManager.Controller;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Enums.EnumTipoTicket;
import com.umari.queueManager.Model.TicketHistorico;
import com.umari.queueManager.service.TicketService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*") // Permite que o frontend (React/Angular/Vue) acesse a API sem erro de CORS
public class TicketController {

    private final TicketService ticketService;

    // Injeção de dependência via construtor
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // 1. Criar Senha (POST)
    // Exemplo de chamada: POST http://localhost:8080/api/tickets?tipo=PRIORIDADE
    // Se não enviar nada, assume NORMAL por padrão.
    @PostMapping
    public Ticket gerarSenha(@RequestParam(defaultValue = "NORMAL") EnumTipoTicket tipoTicket,
                             @RequestParam String nomeCliente) {
        return ticketService.criarSenha(tipoTicket, nomeCliente);
    }

    // 2. Listar Senhas em Espera (GET)
    @GetMapping
    public List<Ticket> listarFila() {
        return ticketService.listarSenhasEmEspera();
    }

    // Adicionar ao TicketController.java

    @PutMapping("/{id}/status")
    public Ticket atualizaStatus(
            @PathVariable String id,
            @RequestParam EnumTickets novoStatus) {
        return ticketService.atualizaStatusTicket(id, novoStatus);
    }

    @PostMapping("/proximo") // POST /api/tickets/proximo
    public Ticket chamarProximoDaFila() {
        return ticketService.chamarProximo();
    }

    @PostMapping("/pausa") // POST /api/tickets/pausa
    public void pausaGeral() {
        ticketService.realizarPausaEArquivar();
    }

    @GetMapping("/historico") // GET /api/tickets/historico
    public List<TicketHistorico> listarHistorico() {
        return ticketService.listarHistorico();
    }

    @GetMapping("/historico/exportar")
    public ResponseEntity<String> exportarHistoricoCsv() {
        List<TicketHistorico> historico = ticketService.listarHistorico();

        StringBuilder csvContent = new StringBuilder();
        // Cabeçalho do CSV
        csvContent.append("ID;Numero;Cliente;Tipo;Data Chegada;Data Atendimento\n");

        for (TicketHistorico t : historico) {
            csvContent.append(t.getId()).append(";")
                    .append(t.getNumero()).append(";")
                    .append(t.getNomeCliente() != null ? t.getNomeCliente() : "Sem Nome").append(";")
                    .append(t.getTipo()).append(";")
                    .append(t.getDataCriacao()).append(";")
                    .append(t.getDataArquivamento()).append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historico_atendimentos.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvContent.toString());
    }

    // Gera o arquivo na memória RAM, sem salvar no disco
    @GetMapping("/historico/baixar-dinamico")
    public ResponseEntity<ByteArrayResource> baixarHistoricoDinamico() {
        List<TicketHistorico> historico = ticketService.listarHistorico();

        StringBuilder csv = new StringBuilder();
        csv.append("ID;Senha;Cliente;Data\n");

        for (TicketHistorico t : historico) {
            csv.append(t.getNumero()).append(";").append(t.getNomeCliente()).append("...\n");
        }

        ByteArrayResource resource = new ByteArrayResource(csv.toString().getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historico.csv")
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
    @PutMapping("/{id}/cancelar") // Define a rota, ex: /atendimentos/5/cancelar
    public ResponseEntity<Void> cancelarAtendimento(@PathVariable String  id) {
        ticketService.atualizaStatusTicket(id, EnumTickets.CANCELADO);
        return ResponseEntity.noContent().build(); // Retorna 204 (Sucesso sem conteúdo)
    }

    @GetMapping("/fila/tamanho")
    public ResponseEntity<Long> contarFila() {
        long qtd = ticketService.listarSenhasEmEspera().size();
        return ResponseEntity.ok(qtd);
    }
}