package br.senai.backendassitencia.service;

import br.senai.backendassitencia.dto.agendamento.AgendamentoCreateDTO;
import br.senai.backendassitencia.dto.agendamento.AgendamentoResponseDTO;
import br.senai.backendassitencia.dto.agendamento.AgendamentoUpdateDTO;
import br.senai.backendassitencia.entity.Agendamento;
import br.senai.backendassitencia.exception.ResourceNotFoundException;
import br.senai.backendassitencia.mapper.AgendamentoMapper;
import br.senai.backendassitencia.repository.AgendamentoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoMapper agendamentoMapper;

    public Page<AgendamentoResponseDTO> listarTodos(Pageable pageable) {
        return agendamentoRepository.findByStatusAgendamentoTrue(pageable)
                .map(agendamentoMapper::toResponse);
    }

    public List<AgendamentoResponseDTO> listarTodosPorCliente(Integer idCliente) {
        return agendamentoMapper.toResponseList(
                agendamentoRepository.findByEquipamento_Cliente_IdClienteAndStatusAgendamentoTrue(idCliente)
        );
    }

    public AgendamentoResponseDTO listarPorId(Integer id) {
        return agendamentoMapper.toResponse(buscarAgendamentoAtivo(id));
    }

    public AgendamentoResponseDTO listarPorIdEquipamento(Integer idEquipamento) {
        return agendamentoRepository.findByEquipamento_IdEquipamentoAndStatusAgendamentoTrue(idEquipamento)
                .map(agendamentoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agendamento não encontrado com ID do Equipamento: " + idEquipamento));
    }

    @Transactional
    public AgendamentoResponseDTO cadastrar(AgendamentoCreateDTO dto) {

        Agendamento agendamento = agendamentoMapper.toEntity(dto);

        return agendamentoMapper.toResponse(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponseDTO atualizar(Integer id, AgendamentoUpdateDTO dto) {
        Agendamento agendamento = buscarAgendamentoAtivo(id);

        agendamentoMapper.updateEntity(dto, agendamento);

        return agendamentoMapper.toResponse(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public void remover(Integer id) {
        Agendamento agendamento = buscarAgendamentoAtivo(id);
        agendamento.setStatusAgendamento(false);
    }

    private Agendamento buscarAgendamentoAtivo(Integer id) {
        return agendamentoRepository.findByIdAgendamentoAndStatusAgendamentoTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Agendamento não encontrado com ID: " + id));
    }
}
