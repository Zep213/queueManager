package com.umari.queueManager.service;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Enums.EnumTipoTicket;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Model.TicketEventoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceTest {

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    WebSocketService webSocketService;

    @Test
    void notificarFila_naoDeveTransmitirNomeClienteNoTopicoPublico() {
        Ticket ticket = new Ticket();
        ticket.setId("id-1");
        ticket.setNumero("N001");
        ticket.setNomeCliente("Fulano de Tal");
        ticket.setStatus(EnumTickets.AGUARDANDO);
        ticket.setTipoTicket(EnumTipoTicket.NORMAL);
        ticket.setCreatedAt(LocalDateTime.now());

        webSocketService.notificarFila(ticket);

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/senhas"), payload.capture());

        assertThat(payload.getValue()).isInstanceOf(TicketEventoDTO.class);
        TicketEventoDTO evento = (TicketEventoDTO) payload.getValue();
        assertThat(evento.numero()).isEqualTo("N001");
        assertThat(evento.toString()).doesNotContain("Fulano de Tal");
    }
}
