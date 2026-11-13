package br.senai.backendassitencia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "equipamento")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEquipamento;

    @Version
    private Long version;

    @NotBlank(message = "O tipo de equipamento é obrigatório")
    @Column(nullable = false, length = 25)
    private String tipo;

    @NotBlank(message = "A marca do equipamento é obrigatório")
    @Column(nullable = false, length = 25)
    private String marca;

    @NotBlank(message = "A marca do equipamento é obrigatório")
    @Column(nullable = false, length = 25)
    private String modelo;

    @Column(nullable = true, length = 25)
    private String numeroSerie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

}
