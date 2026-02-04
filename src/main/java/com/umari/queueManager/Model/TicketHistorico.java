package com.umari.queueManager.Model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "tickets_historico")
public class TicketHistorico {
    @Id
    private String id;
    private String numero;
    private String nomeCliente;
    private String tipo;
    private String statusFinal;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataArquivamento;
    private String atendente;

    public TicketHistorico() {}

    public TicketHistorico(Ticket t) {
        this.id = t.getId();
        this.numero = t.getNumero();
        this.nomeCliente = t.getNomeCliente();
        this.tipo = t.getTipoTicket() != null ? t.getTipoTicket().toString() : "NORMAL";
        this.statusFinal = t.getStatus() != null ? t.getStatus().toString() : "DESCONHECIDO";
        this.dataCriacao = t.getCreatedAt();
        this.dataArquivamento = LocalDateTime.now();
        this.atendente = t.getAtendente();
    }
}