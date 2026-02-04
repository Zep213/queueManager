package com.umari.queueManager.Exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.umari.queueManager.Exceptions.ErroResposta;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> tratarErroArgumento(IllegalArgumentException ex, HttpServletRequest request) {
        return criarResposta(HttpStatus.BAD_REQUEST, "Requisição Inválida", ex.getMessage(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> tratarParametroFaltando(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String mensagem = "O parâmetro obrigatório '" + ex.getParameterName() + "' não foi informado.";
        return criarResposta(HttpStatus.BAD_REQUEST, "Parâmetro em Falta", mensagem, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroGenerico(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();

        return criarResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro Interno", "Ocorreu um erro inesperado no servidor. Tente novamente mais tarde.", request);
    }

    private ResponseEntity<ErroResposta> criarResposta(HttpStatus status, String erro, String mensagem, HttpServletRequest request) {
        ErroResposta corpo = ErroResposta.builder()
                .dataHora(LocalDateTime.now())
                .status(status.value())
                .erro(erro)
                .mensagem(mensagem)
                .caminho(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(corpo);
    }
}