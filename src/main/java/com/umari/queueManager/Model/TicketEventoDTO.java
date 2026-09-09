package com.umari.queueManager.Model;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Enums.EnumTipoTicket;

public record TicketEventoDTO(String id, String numero, EnumTickets status, EnumTipoTicket tipoTicket, String atendente) {
    public static TicketEventoDTO from(Ticket t) {
        return new TicketEventoDTO(t.getId(), t.getNumero(), t.getStatus(), t.getTipoTicket(), t.getAtendente());
    }
}
