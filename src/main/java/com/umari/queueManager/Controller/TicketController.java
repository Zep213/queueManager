package com.umari.queueManager.Controller;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Enums.EnumTipoTicket;
import com.umari.queueManager.Model.TicketHistorico;
import com.umari.queueManager.service.TicketService;
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

    @GetMapping("/historico/baixar-acumulado") // GET /api/tickets/historico/baixar-acumulado
    public ResponseEntity<Object> baixarCsvAcumulado() {
        try {
            // Procura o arquivo na raiz do projeto
            File arquivo = new File("historico_geral.csv");

            if (!arquivo.exists()) {
                return ResponseEntity.badRequest().body("O arquivo ainda não existe. Realize uma pausa para criar o histórico.");
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(arquivo));

            return ResponseEntity.ok()
                    // Força o navegador a baixar com este nome
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"historico_geral.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .contentLength(arquivo.length())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao baixar arquivo: " + e.getMessage());
        }
    }
}