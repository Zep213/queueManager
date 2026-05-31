package com.umari.queueManager;

import com.umari.queueManager.Enums.EnumTickets;
import com.umari.queueManager.Enums.EnumTipoTicket;
import com.umari.queueManager.Exceptions.FilaVaziaException;
import com.umari.queueManager.Exceptions.TicketNotFoundException;
import com.umari.queueManager.Model.Ticket;
import com.umari.queueManager.repository.TicketHistoricoRepository;
import com.umari.queueManager.repository.TicketRepository;
import com.umari.queueManager.service.TicketService;
import com.umari.queueManager.service.WebSocketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoOperations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueManegerApplicationTests {

    @Mock TicketRepository ticketRepository;
    @Mock TicketHistoricoRepository historicoRepository;
    @Mock WebSocketService webSocketService;
    @Mock MongoOperations mongoOperations;

    @InjectMocks TicketService ticketService;

    private Ticket criarTicket(String id, EnumTipoTicket tipo, EnumTickets status) {
        Ticket t = new Ticket();
        t.setId(id);
        t.setNumero(tipo.name().charAt(0) + "001");
        t.setStatus(status);
        t.setTipoTicket(tipo);
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    @Test
    void chamarProximo_deveLancarFilaVaziaQuandoNaoHaTickets() {
        when(ticketRepository.findFirstByStatusAndTipoTicketOrderByCreatedAtAsc(any(), any()))
                .thenReturn(null);

        assertThatThrownBy(() -> ticketService.chamarProximo("guiche01"))
                .isInstanceOf(FilaVaziaException.class)
                .hasMessage("Não há ninguém na fila!");
    }

    @Test
    void chamarProximo_devePriorizarTicketPrioritario() {
        Ticket prioritario = criarTicket("id-p", EnumTipoTicket.PRIORITARIO, EnumTickets.AGUARDANDO);
        when(ticketRepository.findFirstByStatusAndTipoTicketOrderByCreatedAtAsc(
                EnumTickets.AGUARDANDO, EnumTipoTicket.PRIORITARIO))
                .thenReturn(prioritario);
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Ticket resultado = ticketService.chamarProximo("guiche01");

        assertThat(resultado.getStatus()).isEqualTo(EnumTickets.EM_ATENDIMENTO);
        assertThat(resultado.getAtendente()).isEqualTo("guiche01");
        assertThat(resultado.getTipoTicket()).isEqualTo(EnumTipoTicket.PRIORITARIO);
    }

    @Test
    void chamarTicketEspecifico_deveLancarExcecaoSeNaoEncontrado() {
        when(ticketRepository.findById("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.chamarTicketEspecifico("nao-existe", "guiche01"))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void chamarTicketEspecifico_deveRegistrarAtendente() {
        Ticket ticket = criarTicket("id-1", EnumTipoTicket.NORMAL, EnumTickets.AGUARDANDO);
        when(ticketRepository.findById("id-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Ticket resultado = ticketService.chamarTicketEspecifico("id-1", "guiche02");

        assertThat(resultado.getAtendente()).isEqualTo("guiche02");
        assertThat(resultado.getStatus()).isEqualTo(EnumTickets.EM_ATENDIMENTO);
    }

    @Test
    void atualizaStatus_deveRejeitarTransicaoInvalida() {
        Ticket cancelado = criarTicket("id-1", EnumTipoTicket.NORMAL, EnumTickets.CANCELADO);
        when(ticketRepository.findById("id-1")).thenReturn(Optional.of(cancelado));

        assertThatThrownBy(() -> ticketService.atualizaStatusTicket(
                "id-1", EnumTickets.EM_ATENDIMENTO, "guiche01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transição inválida");
    }

    @Test
    void atualizaStatus_devePermitirTransicaoAguardandoParaEmAtendimento() {
        Ticket aguardando = criarTicket("id-1", EnumTipoTicket.NORMAL, EnumTickets.AGUARDANDO);
        when(ticketRepository.findById("id-1")).thenReturn(Optional.of(aguardando));
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Ticket resultado = ticketService.atualizaStatusTicket(
                "id-1", EnumTickets.EM_ATENDIMENTO, "guiche01");

        assertThat(resultado.getStatus()).isEqualTo(EnumTickets.EM_ATENDIMENTO);
    }

    @Test
    void atualizaStatus_deveLancarExcecaoParaTicketInexistente() {
        when(ticketRepository.findById("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.atualizaStatusTicket(
                "fantasma", EnumTickets.ATENDIDO, "guiche01"))
                .isInstanceOf(TicketNotFoundException.class);
    }
}
