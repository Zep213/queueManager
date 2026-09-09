package com.umari.queueManager.service;

import com.umari.queueManager.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service; // Use @Service em vez de @Component

@Service
public class AgendadorLimpeza {

    private static final Logger log = LoggerFactory.getLogger(AgendadorLimpeza.class);

    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    public AgendadorLimpeza(TicketRepository ticketRepository,
                            TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void limparFilaDiaria() {
        log.info("⏰ Iniciando rotina de limpeza noturna...");

        try {
            ticketService.realizarPausaEArquivar();

            if (ticketRepository.count() == 0) {
                ticketService.resetarSequencia();
                log.info("🔄 Contador reiniciado para 001.");
            }
        } catch (Exception e) {
            log.error("❌ Erro na limpeza: ", e);
        }
    }
}