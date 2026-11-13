package br.senai.backendassitencia.dto.agendamento;

import br.senai.backendassitencia.enums.SituacaoAgendamento;

import java.time.LocalDateTime;

public record AgendamentoUpdateDTO(
        LocalDateTime dataAgendamento,
        String descricao,
        SituacaoAgendamento situacaoAgendamento,
        Integer idEquipamento
) {
}
