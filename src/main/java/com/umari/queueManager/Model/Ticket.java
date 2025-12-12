package com.umari.queueManager.Model;


import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Enums.EnumTipoTicket;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Ticket {

    private String id;
    private String numero;
    private String nomeCliente;
    private EnumTickets status;
    private LocalDateTime createdAt;
    private EnumTipoTicket tipoTicket;

    public Ticket(String id, String numero, EnumTickets TicketStatus, LocalDateTime createdAt, EnumTipoTicket tipoTicket){
        this.id = id;
        this.numero = numero;
        this.status = TicketStatus;
        this.createdAt = createdAt;
        this.tipoTicket = tipoTicket;
    }

    public Ticket() {
    }
}
