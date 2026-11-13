package br.senai.backendassitencia.dto.equipamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EquipamentoCreateDTO(
        @NotBlank(message = "O tipo do equipamento é obrigatório")
        String tipo,

        @NotBlank(message = "A marca do equipamento é obrigatória")
        String marca,

        @NotBlank(message = "O modelo do equipamento é obrigatório")
        String modelo,

        String numeroSerie,

        @NotNull(message = "O cliente é obrigatório")
        Integer idCliente
) {
}
