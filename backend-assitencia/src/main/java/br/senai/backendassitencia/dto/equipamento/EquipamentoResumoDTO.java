package br.senai.backendassitencia.dto.equipamento;

public record EquipamentoResumoDTO(
        Integer idEquipamento,
        String tipo,
        String marca,
        String modelo
) {
}
