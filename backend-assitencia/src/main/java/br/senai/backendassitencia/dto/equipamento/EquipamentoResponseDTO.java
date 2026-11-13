package br.senai.backendassitencia.dto.equipamento;

import br.senai.backendassitencia.dto.cliente.ClienteResumoDTO;

public record EquipamentoResponseDTO(
        Integer idEquipamento,
        String tipo,
        String marca,
        String modelo,
        String numeroSerie,
        ClienteResumoDTO cliente
) {
}
