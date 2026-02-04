package com.umari.queueManager.service;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Model.TicketHistorico;
import com.umari.queueManager.repository.TicketHistoricoRepository;
import com.umari.queueManager.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service; // Use @Service em vez de @Component
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendadorLimpeza {

    private static final Logger log = LoggerFactory.getLogger(AgendadorLimpeza.class);

    private final TicketRepository ticketRepository;
    private final TicketHistoricoRepository ticketHistoricoRepository;
    private final TicketService ticketService;

    public AgendadorLimpeza(TicketRepository ticketRepository,
                            TicketHistoricoRepository ticketHistoricoRepository,
                            TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.ticketHistoricoRepository = ticketHistoricoRepository;
        this.ticketService = ticketService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void limparFilaDiaria() {
        log.info("⏰ Iniciando rotina de limpeza noturna...");

        try {
            List<Ticket> finalizados = ticketRepository.findByStatusIn(
                    List.of(EnumTickets.ATENDIDO, EnumTickets.CANCELADO)
            );

            if (!finalizados.isEmpty()) {
                List<TicketHistorico> historico = finalizados.stream().map(t -> {
                    TicketHistorico h = new TicketHistorico();
                    h.setNumero(t.getNumero());
                    h.setNomeCliente(t.getNomeCliente()); // Importante salvar o nome
                    h.setTipo(t.getTipoTicket() != null ? t.getTipoTicket().toString() : "NORMAL");
                    h.setStatusFinal(t.getStatus().toString());
                    h.setDataCriacao(t.getCreatedAt());
                    h.setDataArquivamento(LocalDateTime.now());
                    h.setAtendente(t.getAtendente());
                    return h;
                }).toList();

                ticketHistoricoRepository.saveAll(historico);
                ticketRepository.deleteAll(finalizados);

                log.info("✅ {} tickets movidos para o histórico.", finalizados.size());
            }

            if (ticketRepository.count() == 0) {
                ticketService.resetarSequencia();
                log.info("🔄 Contador reiniciado para 001.");
            }

        } catch (Exception e) {
            log.error("❌ Erro na limpeza: ", e);
        }
    }
}