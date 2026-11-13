package br.senai.backendassitencia.repository;

import br.senai.backendassitencia.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Page<Usuario> findByStatusUsuarioTrue(Pageable pageable);
    Optional<Usuario> findByEmailAndStatusUsuarioTrue(String email);
    Optional<Usuario> findByIdUsuarioAndStatusUsuarioTrue(Integer id);
    boolean existsByEmail(String email);
}

