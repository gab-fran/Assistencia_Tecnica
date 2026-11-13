package br.senai.backendassitencia.dto.agendamento;

import br.senai.backendassitencia.dto.equipamento.EquipamentoResumoDTO;
import br.senai.backendassitencia.enums.SituacaoAgendamento;

import java.time.LocalDateTime;

public record AgendamentoResumoDTO(
        Integer idAgendamento,
        LocalDateTime dataAgendamento,
        SituacaoAgendamento situacaoAgendamento,
        EquipamentoResumoDTO equipamento
) {
}
