package br.senai.backendassitencia.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityAndFilterTest extends BaseControllerTest {

    @Test
    @DisplayName("Filtro JWT - Deve retornar 401 com mensagem padronizada quando header Authorization não inicia com Bearer")
    void deveRetornar401QuandoHeaderNaoIniciaComBearer() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "TokenInvalidoSemBearer"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("O cabeçalho Authorization deve seguir o padrão 'Bearer <token>'")));
    }

    @Test
    @DisplayName("Filtro JWT - Deve retornar 403 quando token Bearer é inválido ou adulterado")
    void deveRetornar403ComTokenBearerInvalido() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer token.completamente.invalido"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CorrelationIdFilter - Deve propagar X-Correlation-Id recebido na requisição")
    void devePropagarCorrelationIdEnviado() throws Exception {
        mockMvc.perform(get("/")
                        .header("X-Correlation-Id", "custom-corr-id-12345"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "custom-corr-id-12345"));
    }

    @Test
    @DisplayName("CorrelationIdFilter - Deve gerar novo X-Correlation-Id quando não enviado")
    void deveGerarNovoCorrelationIdQuandoAusente() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(header().string("X-Correlation-Id", notNullValue()));
    }

    @Test
    @DisplayName("CORS - Deve permitir requisições OPTIONS pré-vôo")
    void devePermitirOptions() throws Exception {
        mockMvc.perform(options("/clientes")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GlobalExceptionHandler - Deve retornar 400 amigável para JSON malformado")
    void deveTratarJsonMalformado() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ email: 'malformado' ,,, }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Corpo da requisição malformado ou inválido.")));
    }
}
