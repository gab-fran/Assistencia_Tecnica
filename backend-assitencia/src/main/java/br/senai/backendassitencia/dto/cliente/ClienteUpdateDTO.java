package br.senai.backendassitencia.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record ClienteUpdateDTO(
        String nome,

        @Email(message = "E-mail inválido")
        String email,

        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos")
        String cpf,

        @Pattern(regexp = "\\d{11}", message = "O celular deve conter 11 dígitos")
        String celular,

        Boolean statusCliente
) {
}
