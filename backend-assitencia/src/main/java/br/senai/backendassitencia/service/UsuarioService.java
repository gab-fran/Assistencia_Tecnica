package br.senai.backendassitencia.service;

import br.senai.backendassitencia.dto.usuario.AlterarSenhaDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioCreateDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioResponseDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioUpdateDTO;
import br.senai.backendassitencia.entity.Usuario;
import br.senai.backendassitencia.enums.RoleUsuario;
import br.senai.backendassitencia.exception.BusinessException;
import br.senai.backendassitencia.exception.ResourceNotFoundException;
import br.senai.backendassitencia.mapper.UsuarioMapper;
import br.senai.backendassitencia.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable) {
        return usuarioRepository.findByStatusUsuarioTrue(pageable)
                .map(usuarioMapper::toResponse);
    }

    public UsuarioResponseDTO listarPorEmail(String email) {
        return usuarioMapper.toResponse(buscarUsuarioAtivo(email));
    }

    public UsuarioResponseDTO listarPorId(Integer id) {
        return usuarioMapper.toResponse(buscarUsuarioAtivo(id));
    }

    @Transactional
    public UsuarioResponseDTO cadastrar(
            UsuarioCreateDTO dto) {

        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe um usuário com este email");
        }

        Usuario usuario = usuarioMapper.toEntity(dto);

        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setRole(dto.role() == null ? RoleUsuario.FUNCIONARIO : dto.role());

        try {
            return usuarioMapper.toResponse(usuarioRepository.saveAndFlush(usuario));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Já existe um usuário com este email");
        }
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Integer id, UsuarioUpdateDTO dto) {
        Usuario usuario = buscarUsuarioAtivo(id);

        usuarioMapper.updateEntity(dto, usuario);

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void alterarSenha(Integer id, AlterarSenhaDTO dto) {
        Usuario usuario = buscarUsuarioAtivo(id);

        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
    }

    @Transactional
    public void remover(Integer id) {
        Usuario usuario = buscarUsuarioAtivo(id);
        usuario.setStatusUsuario(false);
    }

    private Usuario buscarUsuarioAtivo(Integer id) {
        return usuarioRepository.findByIdUsuarioAndStatusUsuarioTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com ID: " + id));
    }

    private Usuario buscarUsuarioAtivo(String email) {
        return usuarioRepository.findByEmailAndStatusUsuarioTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com email: " + email));
    }
}
