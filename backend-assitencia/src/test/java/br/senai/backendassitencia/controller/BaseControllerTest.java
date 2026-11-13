package br.senai.backendassitencia.controller;

import br.senai.backendassitencia.entity.Usuario;
import br.senai.backendassitencia.enums.RoleUsuario;
import br.senai.backendassitencia.repository.UsuarioRepository;
import br.senai.backendassitencia.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@Transactional
public abstract class BaseControllerTest {

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    protected String gerarToken(String email, RoleUsuario role) {
        Usuario usuario = usuarioRepository.findByEmailAndStatusUsuarioTrue(email)
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .nome("Usuario " + role.name())
                        .email(email)
                        .senha(passwordEncoder.encode("senha123456"))
                        .role(role)
                        .statusUsuario(true)
                        .build()));

        return jwtService.generateToken(usuario);
    }

    protected String tokenAdmin() {
        return "Bearer " + gerarToken("admin.test@email.com", RoleUsuario.ADMINISTRADOR);
    }

    protected String tokenFuncionario() {
        return "Bearer " + gerarToken("funcionario.test@email.com", RoleUsuario.FUNCIONARIO);
    }
}
