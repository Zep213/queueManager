package com.umari.queueManager.Exceptions;

import com.umari.queueManager.Exceptions.FilaVaziaException;
import com.umari.queueManager.Exceptions.TicketNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.umari.queueManager.Exceptions.ErroResposta;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResposta> tratarConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String mensagem = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining("; "));
        return criarResposta(HttpStatus.BAD_REQUEST, "Dados Inválidos", mensagem, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return criarResposta(HttpStatus.BAD_REQUEST, "Dados Inválidos", mensagem, request);
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErroResposta> tratarTicketNaoEncontrado(TicketNotFoundException ex, HttpServletRequest request) {
        return criarResposta(HttpStatus.NOT_FOUND, "Ticket Não Encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(FilaVaziaException.class)
    public ResponseEntity<ErroResposta> tratarFilaVazia(FilaVaziaException ex, HttpServletRequest request) {
        return criarResposta(HttpStatus.UNPROCESSABLE_ENTITY, "Fila Vazia", ex.getMessage(), request);
    }

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