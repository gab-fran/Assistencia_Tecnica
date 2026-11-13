package br.senai.backendassitencia.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HomeControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET / - Deve retornar 200 OK e mensagem de boas-vindas sem autenticação")
    void deveRetornarMensagemInicial() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("API Assistência Técnica está funcionando!"));
    }
}
