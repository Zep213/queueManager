package com.umari.queueManager.service;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Model.TicketHistorico;
import com.umari.queueManager.repository.TicketHistoricoRepository;
import com.umari.queueManager.repository.TicketRepository;
import com.umari.queueManager.Enums.EnumTipoTicket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.umari.queueManager.Model.DatabaseSequence;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;

import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketHistoricoRepository historicoRepository;
    private final WebSocketService webSocketService;
    private final MongoOperations mongoOperations;

    public TicketService(TicketRepository ticketRepository,
                         TicketHistoricoRepository ticketHistoricoRepository,
                         WebSocketService webSocketService,
                         MongoOperations mongoOperations) { // <--- Novo argumento
        this.ticketRepository = ticketRepository;
        this.historicoRepository = ticketHistoricoRepository;
        this.webSocketService = webSocketService;
        this.mongoOperations = mongoOperations;
    }

    public Ticket criarSenha(EnumTipoTicket tipoSolicitado, String nomeCliente) {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);

        String prefixo;
        if (tipoSolicitado == EnumTipoTicket.PRIORITARIO) {
            prefixo = "P";
        } else if (tipoSolicitado == EnumTipoTicket.AVULSO) {
            prefixo = "A";
        } else {
            prefixo = "N";
        }

        Ticket ultimaSenhaDesteTipo = ticketRepository.findFirstByTipoTicketAndCreatedAtBetweenOrderByCreatedAtDesc(
                tipoSolicitado, inicioDia, fimDia
        );

        String novoNumero = gerarProximoNumero(ultimaSenhaDesteTipo, prefixo);

        Ticket ticket = new Ticket();
        ticket.setNumero(novoNumero);
        ticket.setNomeCliente(nomeCliente);
        ticket.setStatus(EnumTickets.AGUARDANDO);
        ticket.setTipoTicket(tipoSolicitado);
        ticket.setCreatedAt(LocalDateTime.now());

        webSocketService.notificarFila(ticket);
        return ticketRepository.save(ticket);
    }

    public long generateSequence(String seqName) {
        DatabaseSequence counter = mongoOperations.findAndModify(query(where("_id").is(seqName)),
                new Update().inc("seq", 1), options().returnNew(true).upsert(true),
                DatabaseSequence.class);
        return !Objects.isNull(counter) ? counter.getSeq() : 1;
    }

    public Ticket chamarTicketEspecifico(String id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado!"));

        if (ticket.getStatus() != EnumTickets.AGUARDANDO) {
            throw new RuntimeException("Este ticket não está mais na fila! Status atual: " + ticket.getStatus());
        }

        ticket.setStatus(EnumTickets.EM_ATENDIMENTO);

        webSocketService.notificarFila(ticket);
        return ticketRepository.save(ticket);
    }

    private int gerarProximoNumero() {
        return (int) generateSequence("ticket_sequence");
    }

    public void resetarSequencia() {
        mongoOperations.findAndModify(query(where("_id").is("ticket_sequence")),
                new Update().set("seq", 0), options().returnNew(true).upsert(true),
                DatabaseSequence.class);
    }

    private String gerarProximoNumero(Ticket ultimaSenha, String prefixoAtual) {
        if (ultimaSenha == null) {
            return prefixoAtual + "001";
        }
        String numeroString = ultimaSenha.getNumero().substring(1);
        int sequencial = Integer.parseInt(numeroString);
        sequencial++;
        return String.format("%s%03d", prefixoAtual, sequencial);
    }

    public List<Ticket> listarSenhasEmEspera() {
        return ticketRepository.findByStatus(EnumTickets.AGUARDANDO);
    }
    public List<Ticket> listarTodosAtivos() {
        List<Ticket> lista = new ArrayList<>();
        lista.addAll(ticketRepository.findByStatus(EnumTickets.AGUARDANDO));
        lista.addAll(ticketRepository.findByStatus(EnumTickets.EM_ATENDIMENTO));
        return lista;
    }

    public List<Ticket> gerarRelatorioAtendimentos() {
        return ticketRepository.findByStatus(EnumTickets.ATENDIDO);
    }

    public Ticket atualizaStatusTicket(String ticketId, EnumTickets novoStatus, String nomeAtendente) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado!"));

        ticket.setStatus(novoStatus);
        if (nomeAtendente != null && !nomeAtendente.isEmpty()) {
            ticket.setAtendente(nomeAtendente);
        }
        return ticketRepository.save(ticket);
    }

    public Ticket chamarProximo(String nomeAtendente) {
        Ticket proximo = ticketRepository.findFirstByStatusAndTipoTicketOrderByCreatedAtAsc(
                EnumTickets.AGUARDANDO, EnumTipoTicket.PRIORITARIO);

        if (proximo == null) {
            proximo = ticketRepository.findFirstByStatusAndTipoTicketOrderByCreatedAtAsc(
                    EnumTickets.AGUARDANDO, EnumTipoTicket.NORMAL);
        }

        if (proximo == null) {
            proximo = ticketRepository.findFirstByStatusAndTipoTicketOrderByCreatedAtAsc(
                    EnumTickets.AGUARDANDO, EnumTipoTicket.AVULSO);
        }

        if (proximo == null) {
            throw new RuntimeException("Não há ninguém na fila!");
        }

        proximo.setStatus(EnumTickets.EM_ATENDIMENTO);
        proximo.setAtendente(nomeAtendente);

        webSocketService.notificarFila(proximo);
        return ticketRepository.save(proximo);
    }

    public void realizarPausaEArquivar() {
        List<Ticket> lixo = ticketRepository.findByStatus(EnumTickets.ATENDIDO);
        lixo.addAll(ticketRepository.findByStatus(EnumTickets.CANCELADO));

        if (!lixo.isEmpty()) {
            List<TicketHistorico> historicos = lixo.stream()
                    .map(TicketHistorico::new)
                    .collect(Collectors.toList());

            historicoRepository.saveAll(historicos);
            ticketRepository.deleteAll(lixo);
            log.info("🧹 Pausa Realizada: {} senhas arquivadas.", lixo.size());
        } else {
            log.info("☕ Pausa solicitada, mas sem senhas para arquivar.");
        }
    }

    public List<TicketHistorico> listarHistorico() {
        return historicoRepository.findAll();
    }

    public long contarSenhasHoje() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);
        return ticketRepository.countByCreatedAtBetween(inicioDia, fimDia);
    }

    public Map<String, Object> getDadosDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("filaGeral", ticketRepository.findByStatus(EnumTickets.AGUARDANDO));
        dashboard.put("mesasAtivas", ticketRepository.findByStatus(EnumTickets.EM_ATENDIMENTO));
        return dashboard;
    }

    public String calcularPrevisaoAtendimento(int pessoasNaFila) {
        int minutosParaAdicionar = pessoasNaFila * 10; // 10 min por pessoa
        LocalDateTime dataHora = LocalDateTime.now();

        while (minutosParaAdicionar > 0) {
            if (dataHora.getHour() < 8) {
                dataHora = dataHora.withHour(8).withMinute(0).withSecond(0);
            } else if (dataHora.getHour() >= 12 && dataHora.getHour() < 13) {
                dataHora = dataHora.withHour(13).withMinute(0).withSecond(0);
            } else if (dataHora.getHour() >= 16) {
                dataHora = dataHora.plusDays(1).withHour(8).withMinute(0).withSecond(0);
            }

            LocalDateTime fimDoBloco;
            if (dataHora.getHour() < 12) {
                fimDoBloco = dataHora.withHour(12).withMinute(0).withSecond(0);
            } else {
                fimDoBloco = dataHora.withHour(16).withMinute(0).withSecond(0);
            }

            long minutosNoBloco = java.time.temporal.ChronoUnit.MINUTES.between(dataHora, fimDoBloco);

            if (minutosParaAdicionar <= minutosNoBloco) {
                dataHora = dataHora.plusMinutes(minutosParaAdicionar);
                minutosParaAdicionar = 0;
            } else {
                minutosParaAdicionar -= minutosNoBloco;
                if (dataHora.getHour() < 12) {
                    dataHora = dataHora.withHour(13).withMinute(0).withSecond(0);
                } else {
                    dataHora = dataHora.plusDays(1).withHour(8).withMinute(0).withSecond(0);
                }
            }
        }

        java.time.format.DateTimeFormatter horaFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        String diaStr = dataHora.toLocalDate().equals(java.time.LocalDate.now()) ? "Hoje" : "Amanhã";

        if (!dataHora.toLocalDate().equals(java.time.LocalDate.now()) && !dataHora.toLocalDate().equals(java.time.LocalDate.now().plusDays(1))) {
            diaStr = dataHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
        }

        return diaStr + " às " + dataHora.format(horaFmt);
    }
}