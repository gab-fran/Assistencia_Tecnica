package br.senai.backendassitencia.service;

import br.senai.backendassitencia.dto.usuario.AlterarSenhaDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioCreateDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioResponseDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioUpdateDTO;
import br.senai.backendassitencia.entity.Usuario;
import br.senai.backendassitencia.enums.RoleUsuario;
import br.senai.backendassitencia.exception.BusinessException;
import br.senai.backendassitencia.exception.ResourceNotFoundException;
import br.senai.backendassitencia.mapper.UsuarioMapper;
import br.senai.backendassitencia.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("listarTodos - Deve retornar página de usuários")
    void deveListarTodos() {
        Usuario usuario = Usuario.builder().idUsuario(1).nome("Teste").build();
        UsuarioResponseDTO response = new UsuarioResponseDTO(1, "Teste", "teste@email.com", RoleUsuario.FUNCIONARIO, true);
        Page<Usuario> page = new PageImpl<>(List.of(usuario));

        when(usuarioRepository.findByStatusUsuarioTrue(any(PageRequest.class))).thenReturn(page);
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);

        Page<UsuarioResponseDTO> resultado = usuarioService.listarTodos(PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("cadastrar - Deve lançar BusinessException quando email já existir")
    void deveLancarExcecaoAoCadastrarEmailExistente() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Nome", "duplicado@email.com", "senha123", RoleUsuario.FUNCIONARIO);
        when(usuarioRepository.existsByEmail("duplicado@email.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> usuarioService.cadastrar(dto));
        assertEquals("Já existe um usuário com este email", ex.getMessage());
    }

    @Test
    @DisplayName("cadastrar - Deve capturar DataIntegrityViolationException e lançar BusinessException")
    void deveTratarDataIntegrityAoCadastrar() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Nome", "duplicado@email.com", "senha123", null);
        Usuario usuario = Usuario.builder().email("duplicado@email.com").build();

        when(usuarioRepository.existsByEmail("duplicado@email.com")).thenReturn(false);
        when(usuarioMapper.toEntity(dto)).thenReturn(usuario);
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");
        when(usuarioRepository.saveAndFlush(usuario)).thenThrow(new DataIntegrityViolationException("Unique constraint"));

        BusinessException ex = assertThrows(BusinessException.class, () -> usuarioService.cadastrar(dto));
        assertEquals("Já existe um usuário com este email", ex.getMessage());
    }

    @Test
    @DisplayName("alterarSenha - Deve lançar BadCredentialsException quando senha atual não bater")
    void deveLancarExcecaoQuandoSenhaAtualNaoBater() {
        Usuario usuario = Usuario.builder().idUsuario(1).senha("senhaGravada").build();
        AlterarSenhaDTO dto = new AlterarSenhaDTO("senhaIncorreta", "novaSenha123");

        when(usuarioRepository.findByIdUsuarioAndStatusUsuarioTrue(1)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaIncorreta", "senhaGravada")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> usuarioService.alterarSenha(1, dto));
    }

    @Test
    @DisplayName("remover - Deve alterar statusUsuario para false")
    void deveInativarUsuarioAoRemover() {
        Usuario usuario = Usuario.builder().idUsuario(1).statusUsuario(true).build();
        when(usuarioRepository.findByIdUsuarioAndStatusUsuarioTrue(1)).thenReturn(Optional.of(usuario));

        usuarioService.remover(1);

        assertFalse(usuario.getStatusUsuario());
    }

    @Test
    @DisplayName("buscarUsuarioAtivo por ID - Deve lançar ResourceNotFoundException se não encontrar")
    void deveLancarResourceNotFoundPorId() {
        when(usuarioRepository.findByIdUsuarioAndStatusUsuarioTrue(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.listarPorId(99));
    }

    @Test
    @DisplayName("buscarUsuarioAtivo por email - Deve lançar ResourceNotFoundException se não encontrar")
    void deveLancarResourceNotFoundPorEmail() {
        when(usuarioRepository.findByEmailAndStatusUsuarioTrue("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.listarPorEmail("naoexiste@email.com"));
    }
}
