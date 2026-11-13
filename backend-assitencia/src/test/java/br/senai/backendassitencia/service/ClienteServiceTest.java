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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    private final String chaveHmac = "chave-teste-hmac-123";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(clienteService, "chaveHmac", chaveHmac);
    }

    @Test
    @DisplayName("cadastrar - Deve lançar BusinessException quando email já existir")
    void deveLancarExcecaoQuandoEmailExistir() {
        ClienteCreateDTO dto = new ClienteCreateDTO("Nome", "existe@email.com", "12345678901", "11988887777");
        when(clienteRepository.existsByEmail("existe@email.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> clienteService.cadastrar(dto));
        assertEquals("Já existe um cliente com este email", ex.getMessage());
    }

    @Test
    @DisplayName("cadastrar - Deve lançar BusinessException quando CPF já existir")
    void deveLancarExcecaoQuandoCpfExistir() {
        ClienteCreateDTO dto = new ClienteCreateDTO("Nome", "novo@email.com", "12345678901", "11988887777");
        String hash = HashUtils.gerarHmac("12345678901", chaveHmac);

        when(clienteRepository.existsByEmail("novo@email.com")).thenReturn(false);
        when(clienteRepository.existsByCpfHash(hash)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> clienteService.cadastrar(dto));
        assertEquals("Já existe um cliente com este CPF", ex.getMessage());
    }

    @Test
    @DisplayName("atualizar - Deve lançar BusinessException quando novo CPF já pertencer a outro cliente")
    void deveLancarExcecaoQuandoNovoCpfJaExistir() {
        Cliente cliente = Cliente.builder()
                .idCliente(1)
                .cpf("11111111111")
                .cpfHash(HashUtils.gerarHmac("11111111111", chaveHmac))
                .statusCliente(true)
                .build();

        String novoCpf = "22222222222";
        String novoHash = HashUtils.gerarHmac(novoCpf, chaveHmac);
        ClienteUpdateDTO dto = new ClienteUpdateDTO("Nome", "email@email.com", novoCpf, "11999998888");

        when(clienteRepository.findByIdClienteAndStatusClienteTrue(1)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCpfHash(novoHash)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> clienteService.atualizar(1, dto));
        assertEquals("Já existe um cliente cadastrado com este CPF", ex.getMessage());
    }

    @Test
    @DisplayName("atualizar - Não deve lançar exceção quando CPF for o mesmo")
    void naoDeveLancarExcecaoQuandoCpfForOMesmo() {
        String cpf = "11111111111";
        String hash = HashUtils.gerarHmac(cpf, chaveHmac);
        Cliente cliente = Cliente.builder()
                .idCliente(1)
                .cpf(cpf)
                .cpfHash(hash)
                .statusCliente(true)
                .build();

        ClienteUpdateDTO dto = new ClienteUpdateDTO("Nome", "email@email.com", cpf, "11999998888");
        when(clienteRepository.findByIdClienteAndStatusClienteTrue(1)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toResponse(cliente)).thenReturn(new ClienteResponseDTO(1, "Nome", "email@email.com", cpf, "11999998888", true));

        ClienteResponseDTO response = clienteService.atualizar(1, dto);
        assertEquals(1, response.idCliente());
    }

    @Test
    @DisplayName("buscarClienteAtivoPorEmail - Deve lançar ResourceNotFoundException quando não encontrar")
    void deveLancarExcecaoPorEmailInexistente() {
        when(clienteRepository.findByEmailAndStatusClienteTrue("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.listarPorEmail("naoexiste@email.com"));
    }

    @Test
    @DisplayName("buscarClienteAtivoPorCpf - Deve lançar ResourceNotFoundException quando não encontrar")
    void deveLancarExcecaoPorCpfInexistente() {
        String cpf = "99999999999";
        String hash = HashUtils.gerarHmac(cpf, chaveHmac);
        when(clienteRepository.findByCpfHashAndStatusClienteTrue(hash)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.listarPorCpf(cpf));
    }

    @Test
    @DisplayName("remover - Deve inativar cliente")
    void deveInativarClienteAoRemover() {
        Cliente cliente = Cliente.builder().idCliente(1).statusCliente(true).build();
        when(clienteRepository.findByIdClienteAndStatusClienteTrue(1)).thenReturn(Optional.of(cliente));

        clienteService.remover(1);

        assertFalse(cliente.getStatusCliente());
    }
}
