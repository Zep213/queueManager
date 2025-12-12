package com.umari.queueManager.repository;

import com.umari.queueManager.Model.TicketHistorico;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketHistoricoRepository extends MongoRepository<TicketHistorico, String> {
}