package com.umari.queueManager.Exceptions;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String id) {
        super("Ticket não encontrado: " + id);
    }
}
