package br.senai.backendassitencia.controller;

import br.senai.backendassitencia.dto.usuario.AlterarSenhaDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioCreateDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioUpdateDTO;
import br.senai.backendassitencia.entity.Usuario;
import br.senai.backendassitencia.enums.RoleUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

class UsuarioControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /usuarios - Deve retornar 403 ao tentar acessar sem autenticação")
    void deveRetornarErroSemAutenticacao() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /usuarios - Deve listar usuários paginados quando autenticado")
    void deveListarUsuariosPaginados() throws Exception {
        usuarioRepository.save(Usuario.builder()
                .nome("Usuario Paginado")
                .email("paginado@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", tokenFuncionario())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.pageable", notNullValue()));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - Deve retornar usuário por ID existente")
    void deveBuscarUsuarioPorIdExistente() throws Exception {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome("Usuario Id Teste")
                .email("buscar.id@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        mockMvc.perform(get("/usuarios/" + usuario.getIdUsuario())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario", is(usuario.getIdUsuario())))
                .andExpect(jsonPath("$.email", is("buscar.id@email.com")));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - Deve retornar 404 para ID inexistente")
    void deveRetornar404ParaIdInexistente() throws Exception {
        mockMvc.perform(get("/usuarios/999999")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Usuário não encontrado com ID: 999999")));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - Deve retornar 404 para usuário inativo")
    void deveRetornar404ParaUsuarioInativo() throws Exception {
        Usuario inativo = usuarioRepository.save(Usuario.builder()
                .nome("Usuario Inativo Teste")
                .email("inativo.busca@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(false)
                .build());

        mockMvc.perform(get("/usuarios/" + inativo.getIdUsuario())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /usuarios/por-email/{email} - Deve retornar usuário por email existente")
    void deveBuscarUsuarioPorEmailExistente() throws Exception {
        usuarioRepository.save(Usuario.builder()
                .nome("Usuario Email Teste")
                .email("por.email@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        mockMvc.perform(get("/usuarios/por-email/por.email@email.com")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("por.email@email.com")));
    }

    @Test
    @DisplayName("GET /usuarios/por-email/{email} - Deve retornar 404 para email inexistente")
    void deveRetornar404ParaEmailInexistente() throws Exception {
        mockMvc.perform(get("/usuarios/por-email/naocadastrado@email.com")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Usuário não encontrado com email: naocadastrado@email.com")));
    }

    @Test
    @DisplayName("POST /usuarios - Deve retornar 403 quando chamado por perfil FUNCIONARIO")
    void deveBloquearCadastroPorFuncionario() throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Novo", "novo@email.com", "senha123456", RoleUsuario.FUNCIONARIO);

        mockMvc.perform(post("/usuarios")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", is("Você não tem permissão para acessar este recurso.")));
    }

    @Test
    @DisplayName("POST /usuarios - Deve cadastrar usuário com sucesso quando perfil ADMINISTRADOR")
    void deveCadastrarUsuarioPorAdministrador() throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Novo Admin Criado", "admin.criado@email.com", "senha123456", RoleUsuario.ADMINISTRADOR);

        mockMvc.perform(post("/usuarios")
                        .header("Authorization", tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Novo Admin Criado")))
                .andExpect(jsonPath("$.role", is("ADMINISTRADOR")));
    }

    @Test
    @DisplayName("POST /usuarios - Deve retornar 400 ao tentar cadastrar email duplicado por ADMIN")
    void deveRetornar400AoCadastrarEmailDuplicadoPorAdmin() throws Exception {
        usuarioRepository.save(Usuario.builder()
                .nome("Admin Ja Existe")
                .email("jaexiste@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        UsuarioCreateDTO dto = new UsuarioCreateDTO("Duplicado", "jaexiste@email.com", "senha123456", RoleUsuario.ADMINISTRADOR);

        mockMvc.perform(post("/usuarios")
                        .header("Authorization", tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Já existe um usuário com este email")));
    }

    @Test
    @DisplayName("POST /usuarios - Deve validar campos obrigatórios no cadastro")
    void deveValidarCamposObrigatoriosNoCadastro() throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("", "emailInvalido", "123", null);

        mockMvc.perform(post("/usuarios")
                        .header("Authorization", tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors[*].field", hasItem("nome")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("email")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("senha")));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} - Deve retornar 403 quando chamado por perfil FUNCIONARIO")
    void deveBloquearAtualizacaoPorFuncionario() throws Exception {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Novo Nome", "atualiza@email.com", RoleUsuario.FUNCIONARIO);

        mockMvc.perform(put("/usuarios/1")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} - Deve atualizar usuário com sucesso quando perfil ADMINISTRADOR")
    void deveAtualizarUsuarioPorAdministrador() throws Exception {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome("Nome Antigo")
                .email("antigo@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Nome Atualizado", "novo.email@email.com", RoleUsuario.ADMINISTRADOR);

        mockMvc.perform(put("/usuarios/" + usuario.getIdUsuario())
                        .header("Authorization", tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Nome Atualizado")))
                .andExpect(jsonPath("$.email", is("novo.email@email.com")))
                .andExpect(jsonPath("$.role", is("ADMINISTRADOR")));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} - Deve retornar 404 ao atualizar usuário inexistente")
    void deveRetornar404AoAtualizarInexistente() throws Exception {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Nome", "email@email.com", RoleUsuario.FUNCIONARIO);

        mockMvc.perform(put("/usuarios/999999")
                        .header("Authorization", tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} - Deve validar campos obrigatórios na atualização")
    void deveValidarCamposNaAtualizacao() throws Exception {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("", "emailInvalido", null);

        mockMvc.perform(put("/usuarios/1")
                        .header("Authorization", tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("nome")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("email")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("role")));
    }

    @Test
    @DisplayName("PUT /usuarios/{id}/senha - Deve alterar senha com sucesso quando senha atual estiver correta")
    void deveAlterarSenhaComSucesso() throws Exception {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome("Usuario Troca Senha")
                .email("trocasenha@email.com")
                .senha(passwordEncoder.encode("senhaAtual123"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        AlterarSenhaDTO dto = new AlterarSenhaDTO("senhaAtual123", "novaSenha123456");

        mockMvc.perform(put("/usuarios/" + usuario.getIdUsuario() + "/senha")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        Usuario atualizado = usuarioRepository.findById(usuario.getIdUsuario()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("novaSenha123456", atualizado.getSenha()));
    }

    @Test
    @DisplayName("PUT /usuarios/{id}/senha - Deve retornar 401 ao informar senha atual incorreta")
    void deveRetornar401AoAlterarSenhaComSenhaAtualIncorreta() throws Exception {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome("Usuario Senha Errada")
                .email("senhaerrada@email.com")
                .senha(passwordEncoder.encode("senhaAtual123"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        AlterarSenhaDTO dto = new AlterarSenhaDTO("senhaErrada", "novaSenha123456");

        mockMvc.perform(put("/usuarios/" + usuario.getIdUsuario() + "/senha")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Usuário ou senha inválidos.")));
    }

    @Test
    @DisplayName("PUT /usuarios/{id}/senha - Deve retornar 404 ao alterar senha de usuário inexistente")
    void deveRetornar404AoAlterarSenhaDeUsuarioInexistente() throws Exception {
        AlterarSenhaDTO dto = new AlterarSenhaDTO("senhaAtual123", "novaSenha123456");

        mockMvc.perform(put("/usuarios/999999/senha")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("PUT /usuarios/{id}/senha - Deve validar campos obrigatórios e tamanho da nova senha")
    void deveValidarCamposNaAlteracaoDeSenha() throws Exception {
        AlterarSenhaDTO dto = new AlterarSenhaDTO("", "curta");

        mockMvc.perform(put("/usuarios/1/senha")
                        .header("Authorization", tokenFuncionario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("senhaAtual")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("novaSenha")));
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} - Deve inativar logicamente o usuário existente")
    void deveInativarUsuarioLogicamente() throws Exception {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome("Usuario Para Deletar")
                .email("deletar@email.com")
                .senha(passwordEncoder.encode("senha123456"))
                .role(RoleUsuario.FUNCIONARIO)
                .statusUsuario(true)
                .build());

        mockMvc.perform(delete("/usuarios/" + usuario.getIdUsuario())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuário deletado com sucesso!"));

        mockMvc.perform(get("/usuarios/" + usuario.getIdUsuario())
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} - Deve retornar 404 ao tentar deletar usuário inexistente")
    void deveRetornar404AoDeletarInexistente() throws Exception {
        mockMvc.perform(delete("/usuarios/999999")
                        .header("Authorization", tokenFuncionario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
