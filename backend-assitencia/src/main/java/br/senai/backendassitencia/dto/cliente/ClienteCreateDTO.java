package br.senai.backendassitencia.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClienteCreateDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos")
        String cpf,

        @NotBlank(message = "O celular é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "O celular deve conter 11 dígitos")
        String celular
) {
}
