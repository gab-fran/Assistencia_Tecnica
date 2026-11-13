package br.senai.backendassitencia.dto.cliente;

public record ClienteResponseDTO(
        Integer idCliente,
        String nome,
        String email,
        String cpf,
        String celular,
        Boolean statusCliente
) {
}
