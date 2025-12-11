package com.umari.queueManager.service;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.repository.TicketRepository;
import com.umari.queueManager.Enums.EnumTipoTicket;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TicketService {

    // Constante para o limite diário (fácil de alterar no futuro)
    private static final int LIMITE_DIARIO = 15;

    private final TicketRepository ticketRepository;

    // Injeção de dependência via construtor (melhor prática)
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket criarSenha(EnumTipoTicket tipoSolicitado) {
        // 1. Definir intervalo do dia
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);

        // 2. Verificar Limite GERAL (Continua a contar TODAS as senhas do dia)
        long totalSenhasHoje = ticketRepository.countByCreatedAtBetween(inicioDia, fimDia);

        // Se quiseres que o AVULSO fure o limite, adiciona: && tipoSolicitado != EnumTipoTicket.AVULSO
        if (totalSenhasHoje >= 15) {
            throw new RuntimeException("Limite diário de atendimentos atingido!");
        }

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
        ticket.setStatus(EnumTickets.AGUARDANDO);
        ticket.setTipoTicket(tipoSolicitado);
        ticket.setCreatedAt(LocalDateTime.now());

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

    public void chamaProximoTicket(){
        if (listarSenhasEmEspera().isEmpty()) {
            throw new RuntimeException("Nenhum ticket em espera!");
        } else {
            Ticket ticket = listarSenhasEmEspera().get(0);
            ticket.setStatus(EnumTickets.EM_ATENDIMENTO);
            ticketRepository.save(ticket);
        }
    }
}