package com.umari.queueManager.repository;

import com.umari.queueManager.Enums.EnumTipoTicket;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Enums.EnumTickets;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    // Busca simples por um status
    List<Ticket> findByStatus(EnumTickets status);

    // Permite buscar por uma LISTA de status (ex: ATENDIDO e CANCELADO ao mesmo tempo)
    List<Ticket>findByStatusIn(Collection<EnumTickets> status);

    // 1. Contar senhas no intervalo
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 2. Encontrar a última senha (para sequencia)
    Ticket findFirstByTipoTicketAndCreatedAtBetweenOrderByCreatedAtDesc(EnumTipoTicket tipoTicket, LocalDateTime start, LocalDateTime end);

    // 3. Encontrar o próximo da fila (FIFO)
    Ticket findFirstByStatusAndTipoTicketOrderByCreatedAtAsc(EnumTickets status, EnumTipoTicket tipoTicket);
}

