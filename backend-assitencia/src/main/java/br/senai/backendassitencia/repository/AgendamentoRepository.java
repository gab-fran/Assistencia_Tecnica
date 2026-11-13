package br.senai.backendassitencia.repository;

import br.senai.backendassitencia.entity.Agendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {
    Page<Agendamento> findByStatusAgendamentoTrue(Pageable pageable);
    List<Agendamento> findByEquipamento_Cliente_IdClienteAndStatusAgendamentoTrue(Integer idCliente);
    Optional<Agendamento> findByIdAgendamentoAndStatusAgendamentoTrue(Integer id);
    Optional<Agendamento> findByEquipamento_IdEquipamentoAndStatusAgendamentoTrue(Integer idEquipamento);
}
