package br.senai.backendassitencia.controller;

import br.senai.backendassitencia.dto.cliente.ClienteCreateDTO;
import br.senai.backendassitencia.dto.cliente.ClienteUpdateDTO;
import br.senai.backendassitencia.entity.Cliente;
import br.senai.backendassitencia.repository.ClienteRepository;
import br.senai.backendassitencia.util.HashUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

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

class ClienteControllerTest extends BaseControllerTest {

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

    @Test
    @DisplayName("GET /clientes - Deve retornar 403 sem autenticação")
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /clientes - Deve listar clientes paginados quando autenticado")
    void deveListarClientesPaginados() throws Exception {
        criarCliente("Cliente A", "clientea@email.com", "11111111111", "11999990001");

        mockMvc.perform(get("/clientes")
                        .header("Authorization", tokenFuncionario())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.pageable", notNullValue()));
    }

    @Test
    @DisplayName("GET /clientes/{id} - Deve retornar cliente por ID existente")
    void deveBuscarClientePorId() throws Exception {
        Cliente cliente = criarCliente("Cliente B", "clienteb@email.com", "22222222222", "11999990002");

        mockMvc.perform(get("/clientes/" + cliente.getIdCliente())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente", is(cliente.getIdCliente())))
                .andExpect(jsonPath("$.email", is("clienteb@email.com")))
                .andExpect(jsonPath("$.cpf", is("22222222222")));
    }

    @Test
    @DisplayName("GET /clientes/{id} - Deve retornar 404 para ID inexistente")
    void deveRetornar404ParaIdInexistente() throws Exception {
        mockMvc.perform(get("/clientes/999999")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Cliente não encontrado com ID: 999999")));
    }

    @Test
    @DisplayName("GET /clientes/{id} - Deve retornar 404 para cliente inativo")
    void deveRetornar404ParaClienteInativo() throws Exception {
        Cliente cliente = criarCliente("Inativo", "inativo@email.com", "33333333333", "11999990003");
        cliente.setStatusCliente(false);
        clienteRepository.save(cliente);

        mockMvc.perform(get("/clientes/" + cliente.getIdCliente())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /clientes/email/{email} - Deve retornar cliente por email")
    void deveBuscarClientePorEmail() throws Exception {
        criarCliente("Cliente Email", "busca.email@email.com", "44444444444", "11999990004");

        mockMvc.perform(get("/clientes/email/busca.email@email.com")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("busca.email@email.com")));
    }

    @Test
    @DisplayName("GET /clientes/email/{email} - Deve retornar 404 para email inexistente")
    void deveRetornar404ParaEmailInexistente() throws Exception {
        mockMvc.perform(get("/clientes/email/naoexiste@email.com")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Cliente não encontrado com E-MAIL: naoexiste@email.com")));
    }

    @Test
    @DisplayName("GET /clientes/cpf/{cpf} - Deve retornar cliente por CPF")
    void deveBuscarClientePorCpf() throws Exception {
        criarCliente("Cliente CPF", "busca.cpf@email.com", "55555555555", "11999990005");

        mockMvc.perform(get("/clientes/cpf/55555555555")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("busca.cpf@email.com")))
                .andExpect(jsonPath("$.cpf", is("55555555555")));
    }

    @Test
    @DisplayName("GET /clientes/cpf/{cpf} - Deve retornar 404 para CPF inexistente")
    void deveRetornar404ParaCpfInexistente() throws Exception {
        mockMvc.perform(get("/clientes/cpf/00000000000")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Cliente não encontrado com CPF: 00000000000")));
    }

    @Test
    @DisplayName("POST /clientes - Deve cadastrar cliente com sucesso")
    void deveCadastrarClienteComSucesso() throws Exception {
        ClienteCreateDTO dto = new ClienteCreateDTO("Novo Cliente", "novo.cliente@email.com", "66666666666", "11999990006");

        mockMvc.perform(post("/clientes")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCliente", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Novo Cliente")))
                .andExpect(jsonPath("$.email", is("novo.cliente@email.com")))
                .andExpect(jsonPath("$.cpf", is("66666666666")))
                .andExpect(jsonPath("$.celular", is("11999990006")));
    }

    @Test
    @DisplayName("POST /clientes - Deve retornar 400 ao cadastrar email duplicado")
    void deveRetornar400ParaEmailDuplicado() throws Exception {
        criarCliente("Cliente Dup Email", "dup.email@email.com", "77777777777", "11999990007");

        ClienteCreateDTO dto = new ClienteCreateDTO("Outro Nome", "dup.email@email.com", "88888888888", "11999990008");

        mockMvc.perform(post("/clientes")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Já existe um cliente com este email")));
    }

    @Test
    @DisplayName("POST /clientes - Deve retornar 400 ao cadastrar CPF duplicado")
    void deveRetornar400ParaCpfDuplicado() throws Exception {
        criarCliente("Cliente Dup CPF", "cliente1.cpf@email.com", "99999999999", "11999990009");

        ClienteCreateDTO dto = new ClienteCreateDTO("Cliente 2 CPF", "cliente2.cpf@email.com", "99999999999", "11999990010");

        mockMvc.perform(post("/clientes")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Já existe um cliente com este CPF")));
    }

    @Test
    @DisplayName("POST /clientes - Deve validar campos obrigatórios e formatos de CPF e celular")
    void deveValidarCamposNoCadastro() throws Exception {
        ClienteCreateDTO dto = new ClienteCreateDTO("", "emailInvalido", "123", "999");

        mockMvc.perform(post("/clientes")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors[*].field", hasItem("nome")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("email")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("cpf")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("celular")));
    }

    @Test
    @DisplayName("PUT /clientes/{id} - Deve atualizar cliente com sucesso")
    void deveAtualizarClienteComSucesso() throws Exception {
        Cliente cliente = criarCliente("Cliente Original", "original@email.com", "12345678901", "11988880001");

        ClienteUpdateDTO dto = new ClienteUpdateDTO("Cliente Modificado", "modificado@email.com", null, "11988880002");

        mockMvc.perform(put("/clientes/" + cliente.getIdCliente())
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Cliente Modificado")))
                .andExpect(jsonPath("$.email", is("modificado@email.com")))
                .andExpect(jsonPath("$.celular", is("11988880002")));
    }

    @Test
    @DisplayName("PUT /clientes/{id} - Deve atualizar CPF para um novo CPF válido")
    void deveAtualizarCpfComSucesso() throws Exception {
        Cliente cliente = criarCliente("Cliente Troca CPF", "trocacpf@email.com", "12345678902", "11988880003");

        ClienteUpdateDTO dto = new ClienteUpdateDTO("Cliente Troca CPF", "trocacpf@email.com", "12345678903", "11988880003");

        mockMvc.perform(put("/clientes/" + cliente.getIdCliente())
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf", is("12345678903")));
    }

    @Test
    @DisplayName("PUT /clientes/{id} - Deve retornar 400 ao tentar atualizar para CPF já utilizado por outro cliente")
    void deveRetornar400AoAtualizarParaCpfJaExistente() throws Exception {
        criarCliente("Cliente 1", "cliente.outro@email.com", "12345678904", "11988880004");
        Cliente cliente2 = criarCliente("Cliente 2", "cliente.alvo@email.com", "12345678905", "11988880005");

        ClienteUpdateDTO dto = new ClienteUpdateDTO("Cliente 2", "cliente.alvo@email.com", "12345678904", "11988880005");

        mockMvc.perform(put("/clientes/" + cliente2.getIdCliente())
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Já existe um cliente cadastrado com este CPF")));
    }

    @Test
    @DisplayName("PUT /clientes/{id} - Deve retornar 404 ao atualizar cliente inexistente")
    void deveRetornar404AoAtualizarInexistente() throws Exception {
        ClienteUpdateDTO dto = new ClienteUpdateDTO("Nome", "email@email.com", "12345678909", "11988880009");

        mockMvc.perform(put("/clientes/999999")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("PUT /clientes/{id} - Deve validar formatos de email, CPF e celular na atualização")
    void deveValidarCamposNaAtualizacao() throws Exception {
        ClienteUpdateDTO dto = new ClienteUpdateDTO("Nome", "emailInvalido", "123", "999");

        mockMvc.perform(put("/clientes/1")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("email")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("cpf")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("celular")));
    }

    @Test
    @DisplayName("DELETE /clientes/{id} - Deve inativar logicamente o cliente existente")
    void deveInativarClienteLogicamente() throws Exception {
        Cliente cliente = criarCliente("Cliente Para Deletar", "deletar.cliente@email.com", "12345678906", "11988880006");

        mockMvc.perform(delete("/clientes/" + cliente.getIdCliente())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(content().string("Cliente deletado com sucesso!"));

        mockMvc.perform(get("/clientes/" + cliente.getIdCliente())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /clientes/{id} - Deve retornar 404 ao deletar cliente inexistente")
    void deveRetornar404AoDeletarInexistente() throws Exception {
        mockMvc.perform(delete("/clientes/999999")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
