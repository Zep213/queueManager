package com.umari.queueManager.Exceptions;

public class FilaVaziaException extends RuntimeException {
    public FilaVaziaException() {
        super("Não há ninguém na fila!");
    }
}
