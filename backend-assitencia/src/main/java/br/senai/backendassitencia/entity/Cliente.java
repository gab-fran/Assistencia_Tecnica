package br.senai.backendassitencia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;

    @Version
    private Long version;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false, length = 20)
    private String nome;

    @Email
    @NotBlank(message = "O e-mail é obrigatório")
    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @NotBlank(message = "O CPF é obrigatório")
    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @NotBlank(message = "O celular é obrigatório")
    @Column(nullable = false, length = 11)
    private String celular;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean statusCliente = true;

}
