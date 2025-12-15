package com.umari.queueManager.service;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.Model.TicketHistorico;
import com.umari.queueManager.repository.TicketHistoricoRepository;
import com.umari.queueManager.repository.TicketRepository;
import com.umari.queueManager.Enums.EnumTipoTicket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TicketService {

    // Constante para o limite diário (fácil de alterar no futuro)
    //private static final int LIMITE_DIARIO = 15;

    private final TicketRepository ticketRepository;
    private final TicketHistoricoRepository historicoRepository;
    private final WebSocketService webSocketService;
    private final CsvLogService csvLogService;

    public TicketService(TicketRepository ticketRepository,
                         TicketHistoricoRepository historicoRepository,
                         WebSocketService webSocketService,
                         CsvLogService csvLogService) {
        this.ticketRepository = ticketRepository;
        this.historicoRepository = historicoRepository;
        this.webSocketService = webSocketService;
        this.csvLogService = csvLogService;
    }

    public Ticket criarSenha(EnumTipoTicket tipoSolicitado, String nomeCliente) {
        // 1. Definir intervalo do dia
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);

        // 2. Verificar Limite GERAL (Continua a contar TODAS as senhas do dia)
        long totalSenhasHoje = ticketRepository.countByCreatedAtBetween(inicioDia, fimDia);

        // Se quiseres que o AVULSO fure o limite, adiciona: && tipoSolicitado != EnumTipoTicket.AVULSO
       /* if (totalSenhasHoje >= 15) {
            throw new RuntimeException("Limite diário de atendimentos atingido!");
        }*/

        // 3. Definir o Prefixo
        String prefixo;
        if (tipoSolicitado == EnumTipoTicket.PRIORITARIO) {
            prefixo = "P";
        } else if (tipoSolicitado == EnumTipoTicket.AVULSO) {
            prefixo = "A";
        } else {
            prefixo = "N";
        }

        // 4. MUDANÇA AQUI: Buscar a última senha DESTE TIPO específico
        Ticket ultimaSenhaDesteTipo = ticketRepository.findFirstByTipoTicketAndCreatedAtBetweenOrderByCreatedAtDesc(
                tipoSolicitado, // <--- Filtramos pelo tipo que o usuário pediu
                inicioDia,
                fimDia
        );

        // A lógica de gerar número mantém-se igual, mas agora baseia-se na sequência correta
        String novoNumero = gerarProximoNumero(ultimaSenhaDesteTipo, prefixo);

        // 5. Criar e salvar
        Ticket ticket = new Ticket();
        ticket.setNumero(novoNumero);
        ticket.setNomeCliente(nomeCliente);
        ticket.setStatus(EnumTickets.AGUARDANDO);
        ticket.setTipoTicket(tipoSolicitado);
        ticket.setCreatedAt(LocalDateTime.now());

        webSocketService.notificarFila(ticket);
        return ticketRepository.save(ticket);
    }

    // Método auxiliar atualizado para aceitar o prefixo novo
    private String gerarProximoNumero(Ticket ultimaSenha, String prefixoAtual) {
        if (ultimaSenha == null) {
            return prefixoAtual + "001";
        }

        // Tira a primeira letra da senha antiga (seja N, P ou A) e pega o número
        // Ex: "N005" vira "005" -> 5
        String numeroString = ultimaSenha.getNumero().substring(1);
        int sequencial = Integer.parseInt(numeroString);

        sequencial++;

        // Cria a nova string com o prefixo NOVO e o número incrementado
        return String.format("%s%03d", prefixoAtual, sequencial);
    }

    // Método auxiliar para listar senhas em espera
    public List<Ticket> listarSenhasEmEspera() {
        return ticketRepository.findByStatus(EnumTickets.AGUARDANDO);
    }

    // Lógica isolada para calcular o próximo número (ex: "A005" -> "A006")
    private String gerarProximoNumero(Ticket ultimaSenha) {
        if (ultimaSenha == null) {
            // Se não houve senha hoje, começa na A001
            return "A001";
        }

        // Pega no número atual (ex: "A005"), remove o "A" e converte para int
        String numeroString = ultimaSenha.getNumero().replace("A", "");
        int sequencial = Integer.parseInt(numeroString);

        // Soma +1
        sequencial++;

        // Formata novamente com 3 dígitos e o prefixo "A"
        return String.format("A%03d", sequencial);
    }

    public Ticket atualizaStatusTicket(String ticketId, EnumTickets novoStatus) {
        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado!"));

        ticket.setStatus(novoStatus);
        return ticketRepository.save(ticket);
    }

    public Ticket chamarProximo() {
        List<Ticket> emAtendimento = ticketRepository.findByStatus(EnumTickets.EM_ATENDIMENTO);

        for (Ticket t : emAtendimento) {
            t.setStatus(EnumTickets.ATENDIDO);
            ticketRepository.save(t);
        }
        // 1. Tenta achar um PRIORITÁRIO que esteja AGUARDANDO
        Ticket proximo = ticketRepository.findFirstByStatusAndTipoTicketOrderByCreatedAtAsc(
                EnumTickets.AGUARDANDO,
                EnumTipoTicket.PRIORITARIO
        );

        // 2. Se não houver Prioritário, tenta um NORMAL
        if (proximo == null) {
            proximo = ticketRepository.findFirstByStatusAndTipoTicketOrderByCreatedAtAsc(
                    EnumTickets.AGUARDANDO,
                    EnumTipoTicket.NORMAL
            );
        }

        // 3. Se não houver Normal, tenta um AVULSO
        if (proximo == null) {
            proximo = ticketRepository.findFirstByStatusAndTipoTicketOrderByCreatedAtAsc(
                    EnumTickets.AGUARDANDO,
                    EnumTipoTicket.AVULSO
            );
        }

        // 4. Se ainda for null, a fila está vazia!
        if (proximo == null) {
            throw new RuntimeException("Não há ninguém na fila!");
        }

        // 5. Atualiza o status para EM_ATENDIMENTO e Salva
        proximo.setStatus(EnumTickets.EM_ATENDIMENTO);
        return ticketRepository.save(proximo);
    }

    public void realizarPausaEArquivar() {
        // 1. Pega os finalizados
        List<Ticket> lixo = ticketRepository.findByStatus(EnumTickets.ATENDIDO);
        lixo.addAll(ticketRepository.findByStatus(EnumTickets.CANCELADO));

        if (!lixo.isEmpty()) {
            // 2. Converte para histórico
            List<TicketHistorico> historicos = lixo.stream()
                    .map(TicketHistorico::new)
                    .collect(Collectors.toList());

            // 3. Salva no Banco de Histórico (MongoDB)
            historicoRepository.saveAll(historicos);

            // 4. ATUALIZA A PLANILHA CSV LOCAL
            csvLogService.registrarNoArquivo(historicos);

            // 5. Limpa a fila principal
            ticketRepository.deleteAll(lixo);

            log.info("🧹 Pausa Realizada: {} senhas foram arquivadas e removidas da fila.", lixo.size());
        } else {
            log.info("☕ Pausa solicitada, mas não havia senhas finalizadas para arquivar.");
        }
    }
    public List<TicketHistorico> listarHistorico() {
        // Podes querer limitar aos últimos 50 ou filtrar por data no futuro
        return historicoRepository.findAll();
    }
}