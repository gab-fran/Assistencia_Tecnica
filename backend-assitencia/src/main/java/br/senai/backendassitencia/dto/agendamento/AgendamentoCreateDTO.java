package br.senai.backendassitencia.dto.agendamento;

import br.senai.backendassitencia.enums.SituacaoAgendamento;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoCreateDTO(
        @NotNull(message = "A data do agendamento é obrigatória")
        LocalDateTime dataAgendamento,

        String descricao,

        @NotNull(message = "A situação do agendamento é obrigatória")
        SituacaoAgendamento situacaoAgendamento,

        @NotNull(message = "O equipamento é obrigatório")
        Integer idEquipamento
) {
}
