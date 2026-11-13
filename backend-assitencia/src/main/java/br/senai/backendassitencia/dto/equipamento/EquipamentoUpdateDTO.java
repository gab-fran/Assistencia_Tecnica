package br.senai.backendassitencia.dto.equipamento;

public record EquipamentoUpdateDTO(
        String tipo,
        String marca,
        String modelo,
        String numeroSerie,
        Integer idCliente
) {
}
