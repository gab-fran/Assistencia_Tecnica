package br.senai.backendassitencia.controller;

import br.senai.backendassitencia.dto.agendamento.AgendamentoCreateDTO;
import br.senai.backendassitencia.dto.agendamento.AgendamentoUpdateDTO;
import br.senai.backendassitencia.entity.Agendamento;
import br.senai.backendassitencia.entity.Cliente;
import br.senai.backendassitencia.entity.Equipamento;
import br.senai.backendassitencia.enums.SituacaoAgendamento;
import br.senai.backendassitencia.repository.AgendamentoRepository;
import br.senai.backendassitencia.repository.ClienteRepository;
import br.senai.backendassitencia.repository.EquipamentoRepository;
import br.senai.backendassitencia.util.HashUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgendamentoControllerTest extends BaseControllerTest {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Value("${app.criptografia.chave-hmac}")
    private String chaveHmac;

    private Equipamento criarEquipamentoComCliente() {
        String cpfLimpo = "99988877766";
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nome("Cliente Agend")
                .email("agend.cliente@email.com")
                .cpf(cpfLimpo)
                .cpfHash(HashUtils.gerarHmac(cpfLimpo, chaveHmac))
                .celular("11988889999")
                .statusCliente(true)
                .build());

        return equipamentoRepository.save(Equipamento.builder()
                .tipo("Notebook")
                .marca("Dell")
                .modelo("Latitude")
                .numeroSerie("SN-AGD-001")
                .cliente(cliente)
                .statusEquipamento(true)
                .build());
    }

    private Agendamento criarAgendamento(Equipamento equipamento, SituacaoAgendamento situacao) {
        return agendamentoRepository.save(Agendamento.builder()
                .dataAgendamento(LocalDateTime.now().plusDays(2))
                .descricao("Troca de tela e limpeza")
                .situacaoAgendamento(situacao)
                .equipamento(equipamento)
                .statusAgendamento(true)
                .build());
    }

    @Test
    @DisplayName("GET /agendamento - Deve retornar 403 sem autenticação")
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(get("/agendamento"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /agendamento - Deve listar agendamentos paginados quando autenticado")
    void deveListarAgendamentosPaginados() throws Exception {
        Equipamento equipamento = criarEquipamentoComCliente();
        criarAgendamento(equipamento, SituacaoAgendamento.PENDENTE);

        mockMvc.perform(get("/agendamento")
                        .header("Authorization", tokenFuncionario())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.pageable", notNullValue()));
    }

    @Test
    @DisplayName("GET /agendamento/{id} - Deve retornar agendamento por ID")
    void deveBuscarAgendamentoPorId() throws Exception {
        Equipamento equipamento = criarEquipamentoComCliente();
        Agendamento agendamento = criarAgendamento(equipamento, SituacaoAgendamento.CONFIRMADO);

        mockMvc.perform(get("/agendamento/" + agendamento.getIdAgendamento())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAgendamento", is(agendamento.getIdAgendamento())))
                .andExpect(jsonPath("$.descricao", is("Troca de tela e limpeza")))
                .andExpect(jsonPath("$.situacaoAgendamento", is("CONFIRMADO")))
                .andExpect(jsonPath("$.equipamento.idEquipamento", is(equipamento.getIdEquipamento())));
    }

    @Test
    @DisplayName("GET /agendamento/{id} - Deve retornar 404 para ID inexistente")
    void deveRetornar404ParaIdInexistente() throws Exception {
        mockMvc.perform(get("/agendamento/999999")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Agendamento não encontrado com ID: 999999")));
    }

    @Test
    @DisplayName("GET /agendamento/{id} - Deve retornar 404 para agendamento inativo")
    void deveRetornar404ParaAgendamentoInativo() throws Exception {
        Equipamento equipamento = criarEquipamentoComCliente();
        Agendamento agendamento = criarAgendamento(equipamento, SituacaoAgendamento.CANCELADA);
        agendamento.setStatusAgendamento(false);
        agendamentoRepository.save(agendamento);

        mockMvc.perform(get("/agendamento/" + agendamento.getIdAgendamento())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /agendamento - Deve cadastrar agendamento com sucesso")
    void deveCadastrarAgendamentoComSucesso() throws Exception {
        Equipamento equipamento = criarEquipamentoComCliente();

        AgendamentoCreateDTO dto = new AgendamentoCreateDTO(
                LocalDateTime.of(2026, 12, 1, 10, 0),
                "Manutenção preventiva",
                SituacaoAgendamento.PENDENTE,
                equipamento.getIdEquipamento()
        );

        mockMvc.perform(post("/agendamento")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAgendamento", notNullValue()))
                .andExpect(jsonPath("$.descricao", is("Manutenção preventiva")))
                .andExpect(jsonPath("$.situacaoAgendamento", is("PENDENTE")))
                .andExpect(jsonPath("$.equipamento.idEquipamento", is(equipamento.getIdEquipamento())));
    }

    @Test
    @DisplayName("POST /agendamento - Deve validar campos obrigatórios no cadastro")
    void deveValidarCamposNoCadastro() throws Exception {
        AgendamentoCreateDTO dto = new AgendamentoCreateDTO(null, null, null, null);

        mockMvc.perform(post("/agendamento")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors[*].field", hasItem("dataAgendamento")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("situacaoAgendamento")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("idEquipamento")));
    }

    @Test
    @DisplayName("PUT /agendamento/{id} - Deve atualizar agendamento com sucesso")
    void deveAtualizarAgendamentoComSucesso() throws Exception {
        Equipamento equipamento = criarEquipamentoComCliente();
        Agendamento agendamento = criarAgendamento(equipamento, SituacaoAgendamento.PENDENTE);

        AgendamentoUpdateDTO dto = new AgendamentoUpdateDTO(
                LocalDateTime.of(2026, 12, 10, 14, 30),
                "Serviço concluído com sucesso",
                SituacaoAgendamento.REALIZADO,
                null
        );

        mockMvc.perform(put("/agendamento/" + agendamento.getIdAgendamento())
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao", is("Serviço concluído com sucesso")))
                .andExpect(jsonPath("$.situacaoAgendamento", is("REALIZADO")));
    }

    @Test
    @DisplayName("PUT /agendamento/{id} - Deve retornar 404 ao atualizar agendamento inexistente")
    void deveRetornar404AoAtualizarInexistente() throws Exception {
        AgendamentoUpdateDTO dto = new AgendamentoUpdateDTO(
                LocalDateTime.now(), "Desc", SituacaoAgendamento.CANCELADA, null);

        mockMvc.perform(put("/agendamento/999999")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("DELETE /agendamento/{id} - Deve inativar logicamente o agendamento existente")
    void deveInativarAgendamentoLogicamente() throws Exception {
        Equipamento equipamento = criarEquipamentoComCliente();
        Agendamento agendamento = criarAgendamento(equipamento, SituacaoAgendamento.PENDENTE);

        mockMvc.perform(delete("/agendamento/" + agendamento.getIdAgendamento())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(content().string("Agendamento deletado com sucesso!"));

        mockMvc.perform(get("/agendamento/" + agendamento.getIdAgendamento())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /agendamento/{id} - Deve retornar 404 ao deletar agendamento inexistente")
    void deveRetornar404AoDeletarInexistente() throws Exception {
        mockMvc.perform(delete("/agendamento/999999")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
