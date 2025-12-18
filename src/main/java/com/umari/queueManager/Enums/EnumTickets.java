package com.umari.queueManager.Enums;

public enum EnumTickets {

    AGUARDANDO(1),
    EM_ATENDIMENTO(2),
    ATENDIDO(3),
    CANCELADO(4);
    private final int code;
    private EnumTickets(int code) {
        this.code = code;
    }
}
