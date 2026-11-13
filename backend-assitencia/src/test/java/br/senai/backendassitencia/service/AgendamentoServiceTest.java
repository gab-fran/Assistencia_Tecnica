package br.senai.backendassitencia.service;

import br.senai.backendassitencia.dto.agendamento.AgendamentoCreateDTO;
import br.senai.backendassitencia.dto.agendamento.AgendamentoResponseDTO;
import br.senai.backendassitencia.dto.agendamento.AgendamentoUpdateDTO;
import br.senai.backendassitencia.entity.Agendamento;
import br.senai.backendassitencia.enums.SituacaoAgendamento;
import br.senai.backendassitencia.exception.ResourceNotFoundException;
import br.senai.backendassitencia.mapper.AgendamentoMapper;
import br.senai.backendassitencia.repository.AgendamentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendamentoMapper agendamentoMapper;

    @InjectMocks
    private AgendamentoService agendamentoService;

    @Test
    @DisplayName("listarTodos - Deve retornar página de agendamentos")
    void deveListarTodos() {
        Agendamento agendamento = Agendamento.builder().idAgendamento(1).build();
        Page<Agendamento> page = new PageImpl<>(List.of(agendamento));

        when(agendamentoRepository.findByStatusAgendamentoTrue(any(PageRequest.class))).thenReturn(page);
        when(agendamentoMapper.toResponse(agendamento)).thenReturn(new AgendamentoResponseDTO(1, LocalDateTime.now(), "Desc", SituacaoAgendamento.PENDENTE, null, true));

        Page<AgendamentoResponseDTO> resultado = agendamentoService.listarTodos(PageRequest.of(0, 10));
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("listarTodosPorCliente - Deve retornar lista de agendamentos do cliente")
    void deveListarTodosPorCliente() {
        Agendamento agendamento = Agendamento.builder().idAgendamento(1).build();
        List<Agendamento> lista = List.of(agendamento);

        when(agendamentoRepository.findByEquipamento_Cliente_IdClienteAndStatusAgendamentoTrue(5)).thenReturn(lista);
        when(agendamentoMapper.toResponseList(lista)).thenReturn(List.of(new AgendamentoResponseDTO(1, LocalDateTime.now(), "Desc", SituacaoAgendamento.PENDENTE, null, true)));

        List<AgendamentoResponseDTO> resultado = agendamentoService.listarTodosPorCliente(5);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("listarPorIdEquipamento - Deve retornar agendamento do equipamento")
    void deveListarPorIdEquipamento() {
        Agendamento agendamento = Agendamento.builder().idAgendamento(1).build();
        when(agendamentoRepository.findByEquipamento_IdEquipamentoAndStatusAgendamentoTrue(10)).thenReturn(Optional.of(agendamento));
        when(agendamentoMapper.toResponse(agendamento)).thenReturn(new AgendamentoResponseDTO(1, LocalDateTime.now(), "Desc", SituacaoAgendamento.PENDENTE, null, true));

        AgendamentoResponseDTO resultado = agendamentoService.listarPorIdEquipamento(10);
        assertNotNull(resultado);
        assertEquals(1, resultado.idAgendamento());
    }

    @Test
    @DisplayName("listarPorIdEquipamento - Deve lançar ResourceNotFoundException quando não encontrar")
    void deveLancarExcecaoPorIdEquipamentoNaoEncontrado() {
        when(agendamentoRepository.findByEquipamento_IdEquipamentoAndStatusAgendamentoTrue(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> agendamentoService.listarPorIdEquipamento(99));
    }

    @Test
    @DisplayName("buscarAgendamentoAtivo - Deve lançar ResourceNotFoundException quando ID não existir")
    void deveLancarExcecaoPorIdInexistente() {
        when(agendamentoRepository.findByIdAgendamentoAndStatusAgendamentoTrue(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> agendamentoService.listarPorId(999));
    }

    @Test
    @DisplayName("remover - Deve inativar agendamento")
    void deveInativarAgendamentoAoRemover() {
        Agendamento agendamento = Agendamento.builder().idAgendamento(1).statusAgendamento(true).build();
        when(agendamentoRepository.findByIdAgendamentoAndStatusAgendamentoTrue(1)).thenReturn(Optional.of(agendamento));

        agendamentoService.remover(1);

        assertFalse(agendamento.isStatusAgendamento());
    }
}
