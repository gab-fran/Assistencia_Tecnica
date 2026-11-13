package br.senai.backendassitencia.service;

import br.senai.backendassitencia.dto.cliente.ClienteCreateDTO;
import br.senai.backendassitencia.dto.cliente.ClienteResponseDTO;
import br.senai.backendassitencia.dto.cliente.ClienteUpdateDTO;
import br.senai.backendassitencia.entity.Cliente;
import br.senai.backendassitencia.exception.BusinessException;
import br.senai.backendassitencia.exception.ResourceNotFoundException;
import br.senai.backendassitencia.mapper.ClienteMapper;
import br.senai.backendassitencia.repository.ClienteRepository;
import br.senai.backendassitencia.util.HashUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;


    @Value("${app.criptografia.chave-hmac}")
    private String chaveHmac;

    public Page<ClienteResponseDTO> listarTodos(Pageable pageable) {
        return clienteRepository.findByStatusClienteTrue(pageable)
                .map(clienteMapper::toResponse);
    }

    public ClienteResponseDTO listarPorId(Integer id) {
        return clienteMapper.toResponse(buscarClienteAtivo(id));
    }

    public ClienteResponseDTO listarPorEmail(String email) {
        return clienteMapper.toResponse(buscarClienteAtivoPorEmail(email));
    }

    public ClienteResponseDTO listarPorCpf(String cpf) {
        return clienteMapper.toResponse(buscarClienteAtivoPorCpf(cpf));
    }

    @Transactional
    public ClienteResponseDTO cadastrar(ClienteCreateDTO dto) {

        if (clienteRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe um cliente com este email");
        }

        String cpfHash = gerarHashCpf(dto.cpf());

        if (clienteRepository.existsByCpfHash(cpfHash)) {
            throw new BusinessException("Já existe um cliente com este CPF");
        }

        Cliente cliente = clienteMapper.toEntity(dto);
        cliente.setCpfHash(cpfHash);

        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponseDTO atualizar(Integer id, ClienteUpdateDTO dto) {
        Cliente cliente = buscarClienteAtivo(id);

        if (dto.cpf() != null && !dto.cpf().isBlank()) {
            String novoCpfHash = gerarHashCpf(dto.cpf());

            if (!novoCpfHash.equals(cliente.getCpfHash())) {

                if (clienteRepository.existsByCpfHash(novoCpfHash)) {
                    throw new BusinessException("Já existe um cliente cadastrado com este CPF");
                }

                cliente.setCpfHash(novoCpfHash);
            }
        }

        clienteMapper.updateEntity(dto, cliente);

        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void remover(Integer id) {
        Cliente cliente = buscarClienteAtivo(id);
        cliente.setStatusCliente(false);
    }

    private Cliente buscarClienteAtivo(Integer id) {
        return clienteRepository.findByIdClienteAndStatusClienteTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cliente não encontrado com ID: " + id));
    }

    private Cliente buscarClienteAtivoPorEmail(String email) {
        return clienteRepository.findByEmailAndStatusClienteTrue(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cliente não encontrado com E-MAIL: " + email));
    }

    private Cliente buscarClienteAtivoPorCpf(String cpf) {
        String cpfHash = gerarHashCpf(cpf);

        return clienteRepository.findByCpfHashAndStatusClienteTrue(cpfHash)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cliente não encontrado com CPF: " + cpf));
    }

    private String gerarHashCpf(String cpf) {
        String cpfLimpo = cpf.replaceAll("\\D", "");
        return HashUtils.gerarHmac(cpfLimpo, chaveHmac);
    }
}
