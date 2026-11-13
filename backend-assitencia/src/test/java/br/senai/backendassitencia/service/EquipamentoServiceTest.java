package br.senai.backendassitencia.service;

import br.senai.backendassitencia.dto.equipamento.EquipamentoCreateDTO;
import br.senai.backendassitencia.dto.equipamento.EquipamentoResponseDTO;
import br.senai.backendassitencia.dto.equipamento.EquipamentoUpdateDTO;
import br.senai.backendassitencia.entity.Equipamento;
import br.senai.backendassitencia.exception.ResourceNotFoundException;
import br.senai.backendassitencia.mapper.EquipamentoMapper;
import br.senai.backendassitencia.repository.EquipamentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
class EquipamentoServiceTest {

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private EquipamentoMapper equipamentoMapper;

    @InjectMocks
    private EquipamentoService equipamentoService;

    @Test
    @DisplayName("listarTodos - Deve retornar página de equipamentos")
    void deveListarTodos() {
        Equipamento equipamento = Equipamento.builder().idEquipamento(1).build();
        Page<Equipamento> page = new PageImpl<>(List.of(equipamento));

        when(equipamentoRepository.findByStatusEquipamentoTrue(any(PageRequest.class))).thenReturn(page);
        when(equipamentoMapper.toResponse(equipamento)).thenReturn(new EquipamentoResponseDTO(1, "Notebook", "Dell", "G15", "SN1", null));

        Page<EquipamentoResponseDTO> resultado = equipamentoService.listarTodos(PageRequest.of(0, 10));
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("listarTodosPorCliente - Deve retornar lista de equipamentos do cliente")
    void deveListarTodosPorCliente() {
        Equipamento equipamento = Equipamento.builder().idEquipamento(1).build();
        List<Equipamento> lista = List.of(equipamento);

        when(equipamentoRepository.findByCliente_IdClienteAndStatusEquipamentoTrue(10)).thenReturn(lista);
        when(equipamentoMapper.toResponseList(lista)).thenReturn(List.of(new EquipamentoResponseDTO(1, "Notebook", "Dell", "G15", "SN1", null)));

        List<EquipamentoResponseDTO> resultado = equipamentoService.listarTodosPorCliente(10);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("listarPorNumeroSerie - Deve lançar ResourceNotFoundException quando não encontrar")
    void deveLancarExcecaoPorNumeroSerieInexistente() {
        when(equipamentoRepository.findByNumeroSerieAndStatusEquipamentoTrue("SN-NAO-EXISTE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> equipamentoService.listarPorNumeroSerie("SN-NAO-EXISTE"));
    }

    @Test
    @DisplayName("buscarEquipamentoAtivo - Deve lançar ResourceNotFoundException quando ID não existir")
    void deveLancarExcecaoPorIdInexistente() {
        when(equipamentoRepository.findByIdEquipamentoAndStatusEquipamentoTrue(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> equipamentoService.listarPorId(999));
    }

    @Test
    @DisplayName("remover - Deve inativar equipamento")
    void deveInativarEquipamentoAoRemover() {
        Equipamento equipamento = Equipamento.builder().idEquipamento(1).statusEquipamento(true).build();
        when(equipamentoRepository.findByIdEquipamentoAndStatusEquipamentoTrue(1)).thenReturn(Optional.of(equipamento));

        equipamentoService.remover(1);

        assertFalse(equipamento.isStatusEquipamento());
    }
}
