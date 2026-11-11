package br.senai.backendassitencia.entity;

import br.senai.backendassitencia.enums.RoleUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Version
    private Long version;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "O email é obrigatório")
    @Column(nullable = false, unique = true, length = 100)
    @Email
    private String email;

    @NotBlank(message = "A senha é obrigatório")
    @Column(nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleUsuario role;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean statusUsuario = true;
}
