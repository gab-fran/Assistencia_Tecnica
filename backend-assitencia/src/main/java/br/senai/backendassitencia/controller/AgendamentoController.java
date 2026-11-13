package br.senai.backendassitencia.controller;

import br.senai.backendassitencia.dto.agendamento.AgendamentoCreateDTO;
import br.senai.backendassitencia.dto.agendamento.AgendamentoResponseDTO;
import br.senai.backendassitencia.dto.agendamento.AgendamentoUpdateDTO;
import br.senai.backendassitencia.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agendamento")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @GetMapping
    public ResponseEntity<Page<AgendamentoResponseDTO>> buscarTodos(Pageable pageable) {
        return ResponseEntity.ok(agendamentoService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(agendamentoService.listarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> cadastrar(@RequestBody @Valid AgendamentoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamentoService.cadastrar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid AgendamentoUpdateDTO dto) {
        return ResponseEntity.ok(agendamentoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> remover(@PathVariable Integer id) {
        agendamentoService.remover(id);
        return ResponseEntity.ok("Agendamento deletado com sucesso!");
    }
}