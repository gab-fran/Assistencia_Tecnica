package br.senai.backendassitencia.controller;

import br.senai.backendassitencia.dto.equipamento.EquipamentoCreateDTO;
import br.senai.backendassitencia.dto.equipamento.EquipamentoUpdateDTO;
import br.senai.backendassitencia.entity.Cliente;
import br.senai.backendassitencia.entity.Equipamento;
import br.senai.backendassitencia.repository.ClienteRepository;
import br.senai.backendassitencia.repository.EquipamentoRepository;
import br.senai.backendassitencia.util.HashUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EquipamentoControllerTest extends BaseControllerTest {

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Value("${app.criptografia.chave-hmac}")
    private String chaveHmac;

    private Cliente criarCliente(String nome, String email, String cpf, String celular) {
        String cpfLimpo = cpf.replaceAll("\\D", "");
        return clienteRepository.save(Cliente.builder()
                .nome(nome)
                .email(email)
                .cpf(cpfLimpo)
                .cpfHash(HashUtils.gerarHmac(cpfLimpo, chaveHmac))
                .celular(celular)
                .statusCliente(true)
                .build());
    }

    private Equipamento criarEquipamento(String tipo, String marca, String modelo, String numeroSerie, Cliente cliente) {
        return equipamentoRepository.save(Equipamento.builder()
                .tipo(tipo)
                .marca(marca)
                .modelo(modelo)
                .numeroSerie(numeroSerie)
                .cliente(cliente)
                .statusEquipamento(true)
                .build());
    }

    @Test
    @DisplayName("GET /equipamentos - Deve retornar 403 sem autenticação")
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(get("/equipamentos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /equipamentos - Deve listar equipamentos paginados quando autenticado")
    void deveListarEquipamentosPaginados() throws Exception {
        Cliente cliente = criarCliente("Cliente Eq", "eq.list@email.com", "11122233344", "11988887701");
        criarEquipamento("Notebook", "Dell", "Inspirion", "SN123456", cliente);

        mockMvc.perform(get("/equipamentos")
                        .header("Authorization", tokenFuncionario())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.pageable", notNullValue()));
    }

    @Test
    @DisplayName("GET /equipamentos/{id} - Deve retornar equipamento por ID")
    void deveBuscarEquipamentoPorId() throws Exception {
        Cliente cliente = criarCliente("Cliente Eq Id", "eq.id@email.com", "22233344455", "11988887702");
        Equipamento equipamento = criarEquipamento("Smartphone", "Apple", "iPhone 13", "SN-IPHONE-13", cliente);

        mockMvc.perform(get("/equipamentos/" + equipamento.getIdEquipamento())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEquipamento", is(equipamento.getIdEquipamento())))
                .andExpect(jsonPath("$.tipo", is("Smartphone")))
                .andExpect(jsonPath("$.marca", is("Apple")))
                .andExpect(jsonPath("$.modelo", is("iPhone 13")))
                .andExpect(jsonPath("$.numeroSerie", is("SN-IPHONE-13")))
                .andExpect(jsonPath("$.cliente.idCliente", is(cliente.getIdCliente())));
    }

    @Test
    @DisplayName("GET /equipamentos/{id} - Deve retornar 404 para ID inexistente")
    void deveRetornar404ParaIdInexistente() throws Exception {
        mockMvc.perform(get("/equipamentos/999999")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Equipamento não encontrado com ID: 999999")));
    }

    @Test
    @DisplayName("GET /equipamentos/{id} - Deve retornar 404 para equipamento inativo")
    void deveRetornar404ParaEquipamentoInativo() throws Exception {
        Cliente cliente = criarCliente("Cliente Inat", "eq.inat@email.com", "33344455566", "11988887703");
        Equipamento equipamento = criarEquipamento("Tablet", "Samsung", "Galaxy Tab", "SN-TAB-01", cliente);
        equipamento.setStatusEquipamento(false);
        equipamentoRepository.save(equipamento);

        mockMvc.perform(get("/equipamentos/" + equipamento.getIdEquipamento())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /equipamentos/cliente/{idCliente} - Deve listar todos os equipamentos do cliente")
    void deveListarEquipamentosPorCliente() throws Exception {
        Cliente cliente = criarCliente("Cliente Multi", "eq.multi@email.com", "44455566677", "11988887704");
        criarEquipamento("Desktop", "Lenovo", "ThinkCentre", "SN-DK-01", cliente);
        criarEquipamento("Monitor", "LG", "UltraWide", "SN-MN-01", cliente);

        mockMvc.perform(get("/equipamentos/cliente/" + cliente.getIdCliente())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipo", notNullValue()));
    }

    @Test
    @DisplayName("GET /equipamentos/numeroSerie/{numeroSerie} - Deve buscar equipamento por número de série")
    void deveBuscarPorNumeroSerie() throws Exception {
        Cliente cliente = criarCliente("Cliente SN", "eq.sn@email.com", "55566677788", "11988887705");
        criarEquipamento("Impressora", "HP", "LaserJet", "SN-HP-9988", cliente);

        mockMvc.perform(get("/equipamentos/numeroSerie/SN-HP-9988")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroSerie", is("SN-HP-9988")))
                .andExpect(jsonPath("$.marca", is("HP")));
    }

    @Test
    @DisplayName("GET /equipamentos/numeroSerie/{numeroSerie} - Deve retornar 404 para número de série inexistente")
    void deveRetornar404ParaNumeroSerieInexistente() throws Exception {
        mockMvc.perform(get("/equipamentos/numeroSerie/SN-INEXISTENTE")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Equipamento não encontrado com Número de Série: SN-INEXISTENTE")));
    }

    @Test
    @DisplayName("POST /equipamentos - Deve cadastrar equipamento com sucesso")
    void deveCadastrarEquipamentoComSucesso() throws Exception {
        Cliente cliente = criarCliente("Cliente Cad", "eq.cad@email.com", "66677788899", "11988887706");

        EquipamentoCreateDTO dto = new EquipamentoCreateDTO(
                "Console",
                "Sony",
                "PlayStation 5",
                "SN-PS5-001",
                cliente.getIdCliente()
        );

        mockMvc.perform(post("/equipamentos")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEquipamento", notNullValue()))
                .andExpect(jsonPath("$.tipo", is("Console")))
                .andExpect(jsonPath("$.marca", is("Sony")))
                .andExpect(jsonPath("$.modelo", is("PlayStation 5")))
                .andExpect(jsonPath("$.cliente.idCliente", is(cliente.getIdCliente())));
    }

    @Test
    @DisplayName("POST /equipamentos - Deve validar campos obrigatórios no cadastro")
    void deveValidarCamposNoCadastro() throws Exception {
        EquipamentoCreateDTO dto = new EquipamentoCreateDTO("", "", "", null, null);

        mockMvc.perform(post("/equipamentos")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors[*].field", hasItem("tipo")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("marca")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("modelo")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("idCliente")));
    }

    @Test
    @DisplayName("PUT /equipamentos/{id} - Deve atualizar equipamento com sucesso")
    void deveAtualizarEquipamentoComSucesso() throws Exception {
        Cliente cliente = criarCliente("Cliente Up", "eq.up@email.com", "77788899900", "11988887707");
        Equipamento equipamento = criarEquipamento("Notebook", "Acer", "Aspire 5", "SN-ACER-01", cliente);

        EquipamentoUpdateDTO dto = new EquipamentoUpdateDTO(
                "Notebook Gamer",
                "Acer",
                "Nitro 5",
                "SN-ACER-02",
                null
        );

        mockMvc.perform(put("/equipamentos/" + equipamento.getIdEquipamento())
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo", is("Notebook Gamer")))
                .andExpect(jsonPath("$.modelo", is("Nitro 5")))
                .andExpect(jsonPath("$.numeroSerie", is("SN-ACER-02")));
    }

    @Test
    @DisplayName("PUT /equipamentos/{id} - Deve retornar 404 ao atualizar equipamento inexistente")
    void deveRetornar404AoAtualizarInexistente() throws Exception {
        EquipamentoUpdateDTO dto = new EquipamentoUpdateDTO("Tipo", "Marca", "Modelo", "SN", null);

        mockMvc.perform(put("/equipamentos/999999")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("DELETE /equipamentos/{id} - Deve inativar logicamente o equipamento existente")
    void deveInativarEquipamentoLogicamente() throws Exception {
        Cliente cliente = criarCliente("Cliente Del", "eq.del@email.com", "88899900011", "11988887708");
        Equipamento equipamento = criarEquipamento("Smartwatch", "Garmin", "Fenix 7", "SN-GARMIN-01", cliente);

        mockMvc.perform(delete("/equipamentos/" + equipamento.getIdEquipamento())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(content().string("Equipamento deletado com sucesso!"));

        mockMvc.perform(get("/equipamentos/" + equipamento.getIdEquipamento())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /equipamentos/{id} - Deve retornar 404 ao deletar equipamento inexistente")
    void deveRetornar404AoDeletarInexistente() throws Exception {
        mockMvc.perform(delete("/equipamentos/999999")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
