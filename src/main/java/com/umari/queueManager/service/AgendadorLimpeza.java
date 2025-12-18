package com.umari.queueManager.service;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Model.TicketHistorico;
import com.umari.queueManager.repository.TicketHistoricoRepository;
import com.umari.queueManager.repository.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AgendadorLimpeza {

    private final TicketRepository ticketRepository;
    private final TicketHistoricoRepository historicoRepository;

    public AgendadorLimpeza(TicketRepository ticketRepository, TicketHistoricoRepository historicoRepository) {
        this.ticketRepository = ticketRepository;
        this.historicoRepository = historicoRepository;
    }

    // Cron: Segundo Minuto Hora Dia Mês DiaSemana
    // "0 0 0 * * *" = Meia-noite de todos os dias
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional // Garante que só apaga se salvar no histórico com sucesso
    public void arquivarSenhasAntigas() {
        System.out.println("🧹 Iniciando limpeza diária de senhas...");

        // 1. Buscar tudo que já foi finalizado (ATENDIDO ou CANCELADO)
        List<Ticket> atendidos = ticketRepository.findByStatus(EnumTickets.ATENDIDO);
        List<Ticket> cancelados = ticketRepository.findByStatus(EnumTickets.CANCELADO);

        atendidos.addAll(cancelados); // Junta tudo numa lista só

        if (atendidos.isEmpty()) {
            log.info("✅ Nenhuma senha para arquivar hoje.");
            return;
        }

        // 2. Converter para o formato de Histórico
        List<TicketHistorico> historicos = atendidos.stream()
                .map(TicketHistorico::new)
                .collect(Collectors.toList());

        // 3. Salvar no Histórico
        historicoRepository.saveAll(historicos);

        // 4. Apagar da Fila Principal
        ticketRepository.deleteAll(atendidos);

        log.info("✨ Limpeza concluída! " + atendidos.size() + " senhas movidas para o histórico.");
    }

    @Scheduled(initialDelay = 300000, fixedDelay = Long.MAX_VALUE)
    public void limpezaNoArranque() {
        log.info("🧹 Verificando lixo antigo após inicialização...");
        arquivarSenhasAntigas();
    }


}