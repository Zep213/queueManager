package com.umari.queueManager.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int LIMITE_POR_MINUTO = 30;
    private static final long JANELA_MS = 60_000;

    private final ConcurrentHashMap<String, long[]> contadores = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;

        String ip = request.getRemoteAddr();
        long agora = System.currentTimeMillis();

        contadores.compute(ip, (k, v) -> {
            if (v == null || agora - v[1] > JANELA_MS) return new long[]{1, agora};
            v[0]++;
            return v;
        });

        if (contadores.get(ip)[0] > LIMITE_POR_MINUTO) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"erro\":\"Limite de requisições excedido. Tente novamente em breve.\"}");
            return false;
        }
        return true;
    }
}
