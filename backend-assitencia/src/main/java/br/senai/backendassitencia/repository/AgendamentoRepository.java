package br.senai.backendassitencia.repository;

import br.senai.backendassitencia.entity.Agendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {
    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente"})
    Page<Agendamento> findByStatusAgendamentoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente"})
    List<Agendamento> findByEquipamento_Cliente_IdClienteAndStatusAgendamentoTrue(Integer idCliente);

    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente"})
    Optional<Agendamento> findByIdAgendamentoAndStatusAgendamentoTrue(Integer id);

    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente"})
    Optional<Agendamento> findByEquipamento_IdEquipamentoAndStatusAgendamentoTrue(Integer idEquipamento);
}
