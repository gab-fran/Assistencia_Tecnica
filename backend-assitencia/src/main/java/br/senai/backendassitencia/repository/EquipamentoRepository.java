package br.senai.backendassitencia.repository;

import br.senai.backendassitencia.entity.Equipamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Integer> {
    Page<Equipamento> findByStatusEquipamentoTrue(Pageable pageable);
    List<Equipamento> findByCliente_IdClienteAndStatusEquipamentoTrue(Integer idCliente);
    Optional<Equipamento> findByIdEquipamentoAndStatusEquipamentoTrue(Integer id);
    Optional<Equipamento> findByNumeroSerieAndStatusEquipamentoTrue(String numeroSerie);
}
