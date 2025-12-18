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

    // 1. Erro de Regra de Negócio (ex: Nome obrigatório, Limite atingido)
    // Apanha o IllegalArgumentException que lançamos no Controller
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> tratarErroArgumento(IllegalArgumentException ex, HttpServletRequest request) {
        return criarResposta(HttpStatus.BAD_REQUEST, "Requisição Inválida", ex.getMessage(), request);
    }

    // 2. Erro de Parâmetro em falta (ex: esqueceu de mandar ?tipoTicket=...)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> tratarParametroFaltando(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String mensagem = "O parâmetro obrigatório '" + ex.getParameterName() + "' não foi informado.";
        return criarResposta(HttpStatus.BAD_REQUEST, "Parâmetro em Falta", mensagem, request);
    }

    // 3. Erro Genérico (Qualquer outra coisa que exploda, ex: NullPointer, Banco fora do ar)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroGenerico(Exception ex, HttpServletRequest request) {
        // Loga o erro no terminal para tu veres (importante!)
        ex.printStackTrace();

        // Devolve uma mensagem genérica para o utilizador não se assustar
        return criarResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro Interno", "Ocorreu um erro inesperado no servidor. Tente novamente mais tarde.", request);
    }

    // Método auxiliar para montar o JSON
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