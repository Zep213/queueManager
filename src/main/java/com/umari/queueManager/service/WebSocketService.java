package com.umari.queueManager.service;

import com.umari.queueManager.Model.Ticket;
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
        messagingTemplate.convertAndSend("/topic/senhas", ticket);
        log.info("📢 WebSocket: Atualização enviada para a fila: " + ticket.getNumero());
    }
}