package br.senai.backendassitencia.repository;

import br.senai.backendassitencia.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Page<Cliente> findByStatusClienteTrue(Pageable pageable);
    Optional<Cliente> findByIdClienteAndStatusClienteTrue(Integer id);
    Optional<Cliente> findByEmailAndStatusClienteTrue(String email);
    Optional<Cliente> findByCpfHashAndStatusClienteTrue(String cpfHash);
    boolean existsByCpfHash(String cpfHash);
    boolean existsByEmail(String email);
}
