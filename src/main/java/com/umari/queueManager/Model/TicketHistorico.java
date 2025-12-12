package com.umari.queueManager.Model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "tickets_historico") // Grava numa tabela separada
public class TicketHistorico {
    @Id
    private String id;
    private String numero;
    private String nomeCliente;
    private String tipo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataArquivamento;

    public TicketHistorico() {}

    // Construtor inteligente: Copia os dados do Ticket normal
    public TicketHistorico(Ticket t) {
        this.id = t.getId();
        this.numero = t.getNumero();
        this.nomeCliente = t.getNomeCliente();
        this.tipo = t.getTipoTicket().toString();
        this.dataCriacao = t.getCreatedAt();
        this.dataArquivamento = LocalDateTime.now(); // Hora que foi arquivado (Hora da Pausa)
    }
}