package br.senai.backendassitencia;

import br.senai.backendassitencia.entity.Agendamento;
import br.senai.backendassitencia.entity.Cliente;
import br.senai.backendassitencia.entity.Equipamento;
import br.senai.backendassitencia.entity.Usuario;
import br.senai.backendassitencia.enums.RoleUsuario;
import br.senai.backendassitencia.enums.SituacaoAgendamento;
import br.senai.backendassitencia.repository.AgendamentoRepository;
import br.senai.backendassitencia.repository.ClienteRepository;
import br.senai.backendassitencia.repository.EquipamentoRepository;
import br.senai.backendassitencia.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CargaDadosIntegrationTest {

    private static final int QUANTIDADE_REGISTROS = 10;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Test
    void deveAdicionarDezRegistrosEmCadaTabela() {
        long usuariosAntes = usuarioRepository.count();
        long clientesAntes = clienteRepository.count();
        long equipamentosAntes = equipamentoRepository.count();
        long agendamentosAntes = agendamentoRepository.count();

        List<Usuario> usuarios = IntStream.range(0, QUANTIDADE_REGISTROS)
                .mapToObj(this::criarUsuario)
                .toList();
        usuarioRepository.saveAllAndFlush(usuarios);

        List<Cliente> clientes = IntStream.range(0, QUANTIDADE_REGISTROS)
                .mapToObj(this::criarCliente)
                .toList();
        clienteRepository.saveAllAndFlush(clientes);

        List<Equipamento> equipamentos = IntStream.range(0, QUANTIDADE_REGISTROS)
                .mapToObj(indice -> criarEquipamento(indice, clientes.get(indice)))
                .toList();
        equipamentoRepository.saveAllAndFlush(equipamentos);

        List<Agendamento> agendamentos = IntStream.range(0, QUANTIDADE_REGISTROS)
                .mapToObj(indice -> criarAgendamento(indice, equipamentos.get(indice)))
                .toList();
        agendamentoRepository.saveAllAndFlush(agendamentos);

        assertEquals(usuariosAntes + QUANTIDADE_REGISTROS, usuarioRepository.count());
        assertEquals(clientesAntes + QUANTIDADE_REGISTROS, clienteRepository.count());
        assertEquals(equipamentosAntes + QUANTIDADE_REGISTROS, equipamentoRepository.count());
        assertEquals(agendamentosAntes + QUANTIDADE_REGISTROS, agendamentoRepository.count());
    }

    private Usuario criarUsuario(int indice) {
        return Usuario.builder()
                .nome("Usuario Teste " + indice)
                .email("usuario." + UUID.randomUUID() + "@teste.com")
                .senha("senha-hash-teste")
                .role(RoleUsuario.FUNCIONARIO)
                .build();
    }

    private Cliente criarCliente(int indice) {
        return Cliente.builder()
                .nome("Cliente " + indice)
                .email("cliente." + UUID.randomUUID() + "@teste.com")
                .cpf(String.format("%011d", indice + 1))
                .cpfHash(UUID.randomUUID().toString().replace("-", ""))
                .celular(String.format("1199999%04d", indice))
                .build();
    }

    private Equipamento criarEquipamento(int indice, Cliente cliente) {
        return Equipamento.builder()
                .tipo("Notebook")
                .marca("Marca " + indice)
                .modelo("Modelo " + indice)
                .numeroSerie(UUID.randomUUID().toString().substring(0, 20))
                .cliente(cliente)
                .build();
    }

    private Agendamento criarAgendamento(int indice, Equipamento equipamento) {
        SituacaoAgendamento[] situacoes = SituacaoAgendamento.values();
        return Agendamento.builder()
                .dataAgendamento(LocalDateTime.now().plusDays(indice))
                .descricao("Agendamento de teste " + indice)
                .situacaoAgendamento(situacoes[indice % situacoes.length])
                .equipamento(equipamento)
                .build();
    }
}
