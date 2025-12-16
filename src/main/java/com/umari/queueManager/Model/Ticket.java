package com.umari.queueManager.Model;


import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Enums.EnumTipoTicket;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Setter
@Getter
@Document(collection = "tickets")
public class Ticket {

    private String id;
    private String numero;
    private String nomeCliente;
    private EnumTickets status;
    private LocalDateTime createdAt;
    private EnumTipoTicket tipoTicket;
    private String atendente;

    public Ticket(String id, String numero, EnumTickets TicketStatus, LocalDateTime createdAt, EnumTipoTicket tipoTicket,String atendente){
        this.id = id;
        this.numero = numero;
        this.status = TicketStatus;
        this.createdAt = createdAt;
        this.tipoTicket = tipoTicket;
        this.atendente = atendente;
    }

    public Ticket() {
    }
}
