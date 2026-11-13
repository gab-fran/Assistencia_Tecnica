package br.senai.backendassitencia.service;

import br.senai.backendassitencia.dto.auth.LoginRequestDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioCreateDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioResponseDTO;
import br.senai.backendassitencia.entity.Usuario;
import br.senai.backendassitencia.enums.RoleUsuario;
import br.senai.backendassitencia.exception.BusinessException;
import br.senai.backendassitencia.mapper.UsuarioMapper;
import br.senai.backendassitencia.repository.UsuarioRepository;
import br.senai.backendassitencia.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("registrar - Deve lançar BusinessException quando email já existir")
    void deveLancarExcecaoAoRegistrarEmailExistente() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Nome", "existe@email.com", "senha123", RoleUsuario.ADMINISTRADOR);
        when(usuarioRepository.existsByEmail("existe@email.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.registrar(dto));
        assertEquals("Já existe um usuário com este email", ex.getMessage());
    }

    @Test
    @DisplayName("registrar - Deve capturar DataIntegrityViolationException e lançar BusinessException")
    void deveTratarDataIntegrityNoRegistro() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Nome", "novo@email.com", "senha123", null);
        Usuario usuario = Usuario.builder().email("novo@email.com").build();

        when(usuarioRepository.existsByEmail("novo@email.com")).thenReturn(false);
        when(usuarioMapper.toEntity(dto)).thenReturn(usuario);
        when(passwordEncoder.encode("senha123")).thenReturn("encodedSenha");
        when(usuarioRepository.saveAndFlush(usuario)).thenThrow(new DataIntegrityViolationException("Unique"));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.registrar(dto));
        assertEquals("Já existe um usuário com este email", ex.getMessage());
    }

    @Test
    @DisplayName("registrar - Deve cadastrar e garantir role FUNCIONARIO")
    void deveCadastrarComSucesso() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Nome", "novo@email.com", "senha123", RoleUsuario.ADMINISTRADOR);
        Usuario usuario = Usuario.builder().email("novo@email.com").build();
        Usuario salvo = Usuario.builder().idUsuario(1).email("novo@email.com").role(RoleUsuario.FUNCIONARIO).build();
        UsuarioResponseDTO response = new UsuarioResponseDTO(1, "Nome", "novo@email.com", RoleUsuario.FUNCIONARIO, true);

        when(usuarioRepository.existsByEmail("novo@email.com")).thenReturn(false);
        when(usuarioMapper.toEntity(dto)).thenReturn(usuario);
        when(passwordEncoder.encode("senha123")).thenReturn("encodedSenha");
        when(usuarioRepository.saveAndFlush(usuario)).thenReturn(salvo);
        when(usuarioMapper.toResponse(salvo)).thenReturn(response);

        UsuarioResponseDTO res = authService.registrar(dto);
        assertNotNull(res);
        assertEquals(RoleUsuario.FUNCIONARIO, res.role());
    }

    @Test
    @DisplayName("login - Deve lançar BadCredentialsException quando usuário não for encontrado ou inativo")
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoNoLogin() {
        LoginRequestDTO dto = new LoginRequestDTO("naoexiste@email.com", "senha123");
        when(usuarioRepository.findByEmailAndStatusUsuarioTrue("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
    }

    @Test
    @DisplayName("login - Deve lançar BadCredentialsException quando senha não bater")
    void deveLancarExcecaoQuandoSenhaNaoBaterNoLogin() {
        Usuario usuario = Usuario.builder().email("usuario@email.com").senha("senhaGravada").build();
        LoginRequestDTO dto = new LoginRequestDTO("usuario@email.com", "senhaIncorreta");

        when(usuarioRepository.findByEmailAndStatusUsuarioTrue("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaIncorreta", "senhaGravada")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
    }

    @Test
    @DisplayName("login - Deve autenticar com sucesso e chamar JwtService")
    void deveRealizarLoginComSucesso() {
        Usuario usuario = Usuario.builder().idUsuario(1).email("usuario@email.com").senha("hash").role(RoleUsuario.FUNCIONARIO).build();
        LoginRequestDTO dto = new LoginRequestDTO("usuario@email.com", "senhaCorreta");

        when(usuarioRepository.findByEmailAndStatusUsuarioTrue("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaCorreta", "hash")).thenReturn(true);
        when(jwtService.generateToken(usuario)).thenReturn("mocked.jwt.token");

        String token = authService.login(dto);
        assertEquals("mocked.jwt.token", token);
    }
}
