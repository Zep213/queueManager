package com.umari.queueManager.service;

import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Model.TicketEventoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notificarFila(Ticket ticket) {
        // /ws-queue é permitAll — nunca inclua nomeCliente (PII) no broadcast público
        messagingTemplate.convertAndSend("/topic/senhas", TicketEventoDTO.from(ticket));
        log.info("📢 WebSocket: Atualização enviada para a fila: " + ticket.getNumero());
    }
}