package com.umari.queueManager.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umari.queueManager.Model.Usuario;
import com.umari.queueManager.config.JwtUtil;
import com.umari.queueManager.repository.UsuarioRepository;
import com.umari.queueManager.service.AutenticacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AutenticacaoService autenticacaoService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void criar_retorna201SemSenhaNaResposta() throws Exception {
        Usuario requisicao = new Usuario();
        requisicao.setUsername("novo.usuario");
        requisicao.setPassword("SenhaForte123!");
        requisicao.setRole("USER");

        Usuario salvo = new Usuario();
        salvo.setId("abc123");
        salvo.setUsername("novo.usuario");
        salvo.setPassword("$2a$10$hashDaSenhaQueNuncaDeveVazar");
        salvo.setRole("USER");

        when(usuarioRepository.findByUsername("novo.usuario")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashDaSenhaQueNuncaDeveVazar");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(salvo);

        mockMvc.perform(post("/api/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.username").value("novo.usuario"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}
