package br.senai.backendassitencia.dto.agendamento;

import br.senai.backendassitencia.dto.equipamento.EquipamentoResumoDTO;
import br.senai.backendassitencia.enums.SituacaoAgendamento;

import java.time.LocalDateTime;

public record AgendamentoResponseDTO(
        Integer idAgendamento,
        LocalDateTime dataAgendamento,
        String descricao,
        SituacaoAgendamento situacaoAgendamento,
        EquipamentoResumoDTO equipamento,
        Boolean statusEquipamento
) {
}
