package com.umari.queueManager.service;

import com.umari.queueManager.Model.Ticket;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notificarFila(Ticket ticket) {
        // Envia o ticket atualizado para o canal "/topic/senhas"
        // Quem estiver na página do Atendente (ou TV) vai receber este JSON instantaneamente
        messagingTemplate.convertAndSend("/topic/senhas", ticket);
        System.out.println("📢 WebSocket: Atualização enviada para a fila: " + ticket.getNumero());
    }
}