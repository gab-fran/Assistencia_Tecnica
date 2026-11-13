package br.senai.backendassitencia.service;

import br.senai.backendassitencia.dto.equipamento.EquipamentoCreateDTO;
import br.senai.backendassitencia.dto.equipamento.EquipamentoResponseDTO;
import br.senai.backendassitencia.dto.equipamento.EquipamentoUpdateDTO;
import br.senai.backendassitencia.entity.Equipamento;
import br.senai.backendassitencia.exception.ResourceNotFoundException;
import br.senai.backendassitencia.mapper.EquipamentoMapper;
import br.senai.backendassitencia.repository.EquipamentoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final EquipamentoMapper equipamentoMapper;

    public Page<EquipamentoResponseDTO> listarTodos(Pageable pageable) {
        return equipamentoRepository.findByStatusEquipamentoTrue(pageable)
                .map(equipamentoMapper::toResponse);
    }

    public List<EquipamentoResponseDTO> listarTodosPorCliente(Integer idCliente) {
        return equipamentoMapper.toResponseList(
                equipamentoRepository.findByCliente_IdClienteAndStatusEquipamentoTrue(idCliente)
        );
    }

    public EquipamentoResponseDTO listarPorId(Integer id) {
        return equipamentoMapper.toResponse(buscarEquipamentoAtivo(id));
    }

    public EquipamentoResponseDTO listarPorNumeroSerie(String numeroSerie) {
        return equipamentoMapper.toResponse(equipamentoRepository
                .findByNumeroSerieAndStatusEquipamentoTrue(numeroSerie)
                .orElseThrow(() ->
                new ResourceNotFoundException(
                        "Equipamento não encontrado com Número de Série: " + numeroSerie))
        );
    }

    @Transactional
    public EquipamentoResponseDTO cadastrar(EquipamentoCreateDTO dto) {

        Equipamento equipamento = equipamentoMapper.toEntity(dto);

        return equipamentoMapper.toResponse(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public EquipamentoResponseDTO atualizar(Integer id, EquipamentoUpdateDTO dto) {
        Equipamento equipamento = buscarEquipamentoAtivo(id);

        equipamentoMapper.updateEntity(dto, equipamento);

        return equipamentoMapper.toResponse(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public void remover(Integer id) {
        Equipamento equipamento = buscarEquipamentoAtivo(id);
        equipamento.setStatusEquipamento(false);
    }

    private Equipamento buscarEquipamentoAtivo(Integer id) {
        return equipamentoRepository.findByIdEquipamentoAndStatusEquipamentoTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Equipamento não encontrado com ID: " + id));
    }
}

