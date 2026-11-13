package br.senai.backendassitencia.controller;

import br.senai.backendassitencia.dto.auth.LoginRequestDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioCreateDTO;
import br.senai.backendassitencia.entity.Usuario;
import br.senai.backendassitencia.enums.RoleUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("POST /auth/login - Deve autenticar com sucesso e retornar token Bearer")
    void deveFazerLoginComSucesso() throws Exception {
        usuarioRepository.save(Usuario.builder()
                .nome("Usuario Teste")
                .email("login.sucesso@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        LoginRequestDTO request = new LoginRequestDTO("login.sucesso@email.com", "senha123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type", is("Bearer")));
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 401 com senha incorreta")
    void deveRetornar401ComSenhaIncorreta() throws Exception {
        usuarioRepository.save(Usuario.builder()
                .nome("Usuario Teste")
                .email("login.senhaerrada@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        LoginRequestDTO request = new LoginRequestDTO("login.senhaerrada@email.com", "senhaIncorreta");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Usuário ou senha inválidos.")));
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 401 para email inexistente")
    void deveRetornar401ParaEmailInexistente() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("naoexiste@email.com", "senha123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Usuário ou senha inválidos.")));
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 401 para usuário inativo")
    void deveRetornar401ParaUsuarioInativo() throws Exception {
        usuarioRepository.save(Usuario.builder()
                .nome("Usuario Inativo")
                .email("inativo@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(false)
                .build());

        LoginRequestDTO request = new LoginRequestDTO("inativo@email.com", "senha123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Usuário ou senha inválidos.")));
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 400 para campos obrigatórios ausentes ou inválidos")
    void deveValidarCamposObrigatoriosNoLogin() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("emailInvalidoSemArroba", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors[*].field", hasItem("email")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("senha")));
    }

    @Test
    @DisplayName("POST /auth/register - Deve cadastrar novo usuário com perfil FUNCIONARIO")
    void deveRegistrarUsuarioComSucesso() throws Exception {
        UsuarioCreateDTO request = new UsuarioCreateDTO(
                "Novo Usuario",
                "novo.usuario@email.com",
                "senha123456",
                RoleUsuario.ADMINISTRADOR // mesmo enviando ADMIN, o endpoint deve forçar FUNCIONARIO
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Novo Usuario")))
                .andExpect(jsonPath("$.email", is("novo.usuario@email.com")))
                .andExpect(jsonPath("$.role", is("FUNCIONARIO")))
                .andExpect(jsonPath("$.statusUsuario", is(true)));
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 400 ao tentar registrar email duplicado")
    void deveRetornar400AoRegistrarEmailDuplicado() throws Exception {
        usuarioRepository.save(Usuario.builder()
                .nome("Usuario Existente")
                .email("existente@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        UsuarioCreateDTO request = new UsuarioCreateDTO(
                "Outro Usuario",
                "existente@email.com",
                "senha123456",
                RoleUsuario.FUNCIONARIO
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Já existe um usuário com este email")));
    }

    @Test
    @DisplayName("POST /auth/register - Deve validar campos obrigatórios e tamanho da senha")
    void deveValidarCamposObrigatoriosNoRegistro() throws Exception {
        UsuarioCreateDTO request = new UsuarioCreateDTO("", "email-invalido", "123", null);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors[*].field", hasItem("nome")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("email")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("senha")));
    }
}
