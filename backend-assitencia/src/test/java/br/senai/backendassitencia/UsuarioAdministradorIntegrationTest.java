package br.senai.backendassitencia;

import br.senai.backendassitencia.entity.Usuario;
import br.senai.backendassitencia.enums.RoleUsuario;
import br.senai.backendassitencia.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class UsuarioAdministradorIntegrationTest {

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deveAdicionarUsuarioAdministrador() {
        if (usuarioRepository.existsByEmail(adminEmail)) {
            Usuario existente = usuarioRepository.findByEmailAndStatusUsuarioTrue(adminEmail)
                    .orElseThrow(() -> new IllegalStateException(
                            "O e-mail configurado já pertence a um usuário inativo."));
            assertEquals(RoleUsuario.ADMINISTRADOR, existente.getRole());
            return;
        }

        Usuario administrador = Usuario.builder()
                .nome("Administrador")
                .email(adminEmail)
                .senha(passwordEncoder.encode(adminPassword))
                .role(RoleUsuario.ADMINISTRADOR)
                .build();

        Usuario salvo = usuarioRepository.saveAndFlush(administrador);

        assertNotNull(salvo.getIdUsuario());
        assertEquals(RoleUsuario.ADMINISTRADOR, salvo.getRole());
        assertEquals(adminEmail, salvo.getEmail());
    }
}
