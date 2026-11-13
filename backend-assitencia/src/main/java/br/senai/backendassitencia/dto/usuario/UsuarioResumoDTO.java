package br.senai.backendassitencia.dto.usuario;

import br.senai.backendassitencia.enums.RoleUsuario;

public record UsuarioResumoDTO(
        Integer idUsuario,
        String nome,
        String email,
        RoleUsuario role
) {
}
