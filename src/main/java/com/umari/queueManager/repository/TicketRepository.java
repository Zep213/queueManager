package com.umari.queueManager.repository;

import com.umari.queueManager.Enums.EnumTipoTicket;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Enums.EnumTickets;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    List<Ticket> findByStatus(EnumTickets status);

    // 1. Para contar quantas senhas existem num intervalo de tempo
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 2. Para encontrar a ÚLTIMA senha criada num intervalo (para saberes o número anterior)
    Ticket findFirstByTipoTicketAndCreatedAtBetweenOrderByCreatedAtDesc(EnumTipoTicket tipoTicket, LocalDateTime start, LocalDateTime end);

    //3. Encontra o PRIMEIRO (mais antigo) com certo STATUS e TIPO
    Ticket findFirstByStatusAndTipoTicketOrderByCreatedAtAsc(EnumTickets status, EnumTipoTicket tipoTicket);
}
