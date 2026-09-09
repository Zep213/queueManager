package com.umari.queueManager.security;

import com.umari.queueManager.Controller.TicketController;
import com.umari.queueManager.config.JwtFilter;
import com.umari.queueManager.config.JwtUtil;
import com.umari.queueManager.config.SecurityConfig;
import com.umari.queueManager.service.AutenticacaoService;
import com.umari.queueManager.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class TicketSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AutenticacaoService autenticacaoService;

    @Test
    @WithMockUser(roles = "USER")
    void pausaGeral_comRoleUser_retorna403() throws Exception {
        mockMvc.perform(post("/api/tickets/pausa"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void pausaGeral_comRoleAdmin_naoRetorna403() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tickets/pausa"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }
}
