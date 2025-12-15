package com.umari.queueManager.Exceptions;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErroResposta {
    private LocalDateTime dataHora;
    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
}