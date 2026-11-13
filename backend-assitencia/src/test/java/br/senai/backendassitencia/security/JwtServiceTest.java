package br.senai.backendassitencia.security;

import br.senai.backendassitencia.entity.Usuario;
import br.senai.backendassitencia.enums.RoleUsuario;
import br.senai.backendassitencia.exception.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "chave-secreta-de-teste-com-mais-de-32-caracteres!";

    @BeforeEach
    void setup() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationTime", 3600L);
    }

    @Test
    @DisplayName("validateConfig - Deve lançar IllegalStateException se secret for nulo ou vazio")
    void deveValidarConfigSecret() {
        JwtService invalido = new JwtService();
        ReflectionTestUtils.setField(invalido, "secret", "");
        assertThrows(IllegalStateException.class, invalido::validateConfig);

        ReflectionTestUtils.setField(invalido, "secret", null);
        assertThrows(IllegalStateException.class, invalido::validateConfig);
    }

    @Test
    @DisplayName("generateToken - Deve gerar token válido com subject e role")
    void deveGerarTokenValido() {
        Usuario usuario = Usuario.builder()
                .idUsuario(12)
                .email("jwt.teste@email.com")
                .role(RoleUsuario.ADMINISTRADOR)
                .build();

        String token = jwtService.generateToken(usuario);
        assertNotNull(token);

        String subject = jwtService.getSubject(token);
        String role = jwtService.getRole(token);

        assertEquals("jwt.teste@email.com", subject);
        assertEquals("ADMINISTRADOR", role);
    }

    @Test
    @DisplayName("getClaims - Deve lançar InvalidTokenException para token inválido")
    void deveLancarInvalidTokenExceptionParaTokenInvalido() {
        assertThrows(InvalidTokenException.class, () -> jwtService.getClaims("token.falso.invalido"));
    }
}
