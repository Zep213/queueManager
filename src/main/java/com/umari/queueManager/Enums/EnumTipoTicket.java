package com.umari.queueManager.Enums;

public enum EnumTipoTicket {
    NORMAL(1),
    PRIORITARIO(2),
    AVULSO(3);
    private final int code;
    private EnumTipoTicket(int code) {
        this.code = code;
    }
}
