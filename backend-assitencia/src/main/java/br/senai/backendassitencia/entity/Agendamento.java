package br.senai.backendassitencia.entity;

import br.senai.backendassitencia.enums.SituacaoAgendamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAgendamento;

    @Version
    private Long version;

    private LocalDateTime dataAgendamento;

    @Column(length = 255)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "A situação do agendamento é obrigatória")
    private SituacaoAgendamento situacaoAgendamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipamento", nullable = false)
    private Equipamento equipamento;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean statusEquipamento = true;
}