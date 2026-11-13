package br.senai.backendassitencia.repository;

import br.senai.backendassitencia.entity.Equipamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Integer> {
    @EntityGraph(attributePaths = {"cliente"})
    Page<Equipamento> findByStatusEquipamentoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"cliente"})
    List<Equipamento> findByCliente_IdClienteAndStatusEquipamentoTrue(Integer idCliente);

    @EntityGraph(attributePaths = {"cliente"})
    Optional<Equipamento> findByIdEquipamentoAndStatusEquipamentoTrue(Integer id);

    @EntityGraph(attributePaths = {"cliente"})
    Optional<Equipamento> findByNumeroSerieAndStatusEquipamentoTrue(String numeroSerie);
}
