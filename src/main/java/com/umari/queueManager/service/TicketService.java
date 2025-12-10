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
        // 1. Definir intervalo do dia (para o limite e sequencial)
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);

        // 2. Verificar Limite (Exemplo: Limite GERAL de 15, independente do tipo)
        long totalSenhasHoje = ticketRepository.countByCreatedAtBetween(inicioDia, fimDia);
        if (totalSenhasHoje >= 15) {
            throw new RuntimeException("Limite diário de atendimentos atingido!");
        }

        // 3. Definir o Prefixo com base no Tipo
        String prefixo;
        if (tipoSolicitado == EnumTipoTicket.PRIORITARIO) {
            prefixo = "P";
        } else if (tipoSolicitado == EnumTipoTicket.AVULSO) {
            prefixo = "A";
        } else {
            prefixo = "N"; // N de Normal
        }

        // 4. Gerar o próximo número
        // Nota: Aqui podes querer procurar a última senha *deste tipo* ou *geral*.
        // Para simplificar, vamos manter a sequencial geral, mudando só a letra.
        Ticket ultimaSenha = ticketRepository.findFirstByCreatedAtBetweenOrderByCreatedAtDesc(inicioDia, fimDia);
        String novoNumero = gerarProximoNumero(ultimaSenha, prefixo);

        // 5. Criar o objeto Ticket
        Ticket ticket = new Ticket();
        ticket.setNumero(novoNumero);
        ticket.setStatus(EnumTickets.AGUARDANDO);
        ticket.setTipoTicket(tipoSolicitado);      // <--- Gravamos o tipo no banco
        ticket.setCreatedAt(LocalDateTime.now()); // <--- Data atual correta

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
}