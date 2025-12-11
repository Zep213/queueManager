package com.umari.queueManager.Controller;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Enums.EnumTipoTicket;
import com.umari.queueManager.service.TicketService;
import org.springframework.web.bind.annotation.*;

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
    public Ticket gerarSenha(@RequestParam(defaultValue = "NORMAL") EnumTipoTicket tipoTicket) {
        return ticketService.criarSenha(tipoTicket);
    }

    // 2. Listar Senhas em Espera (GET)
    @GetMapping
    public List<Ticket> listarFila() {
        return ticketService.listarSenhasEmEspera();
    }

    @PutMapping("/{id}/status")
    public Ticket atualizaTicket(String id, EnumTickets status) {
        return ticketService.atualizaStatusTicket(id, status);
    }

    @PatchMapping
    public void chamaProximoTicket() {
        ticketService.chamaProximoTicket();
    }
}