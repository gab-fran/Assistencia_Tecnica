package br.senai.backendassitencia.controller;

import br.senai.backendassitencia.dto.equipamento.EquipamentoCreateDTO;
import br.senai.backendassitencia.dto.equipamento.EquipamentoResponseDTO;
import br.senai.backendassitencia.dto.equipamento.EquipamentoUpdateDTO;
import br.senai.backendassitencia.service.EquipamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipamentos")
@RequiredArgsConstructor
public class EquipamentoController {

    private final EquipamentoService equipamentoService;

    @GetMapping
    public ResponseEntity<Page<EquipamentoResponseDTO>> buscarTodos(Pageable pageable) {
        return ResponseEntity.ok(equipamentoService.listarTodos(pageable));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<EquipamentoResponseDTO>> buscarClientePorId(@PathVariable Integer idCliente) {
        return ResponseEntity.ok(equipamentoService.listarTodosPorCliente(idCliente));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(equipamentoService.listarPorId(id));
    }

    @GetMapping("/numeroSerie/{numeroSerie}")
    public ResponseEntity<EquipamentoResponseDTO> buscarPorNumeroSerie(@PathVariable String numeroSerie) {
        return ResponseEntity.ok(equipamentoService.listarPorNumeroSerie(numeroSerie));
    }

    @PostMapping
    public ResponseEntity<EquipamentoResponseDTO> cadastrar(@RequestBody @Valid EquipamentoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipamentoService.cadastrar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid EquipamentoUpdateDTO dto) {
        return ResponseEntity.ok(equipamentoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> remover(@PathVariable Integer id) {
        equipamentoService.remover(id);
        return ResponseEntity.ok("Equipamento deletado com sucesso!");
    }

}