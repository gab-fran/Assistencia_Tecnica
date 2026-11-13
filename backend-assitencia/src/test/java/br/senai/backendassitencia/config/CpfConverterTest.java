package br.senai.backendassitencia.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CpfConverterTest {

    private CpfConverter cpfConverter;

    @BeforeEach
    void setup() {
        cpfConverter = new CpfConverter("senha-teste-123456", "5c0744940b5c369b");
    }

    @Test
    @DisplayName("convertToDatabaseColumn - Deve retornar null quando entrada for nula")
    void deveRetornarNullAoCriptografarNulo() {
        assertNull(cpfConverter.convertToDatabaseColumn(null));
    }

    @Test
    @DisplayName("convertToEntityAttribute - Deve retornar null quando entrada for nula")
    void deveRetornarNullAoDescriptografarNulo() {
        assertNull(cpfConverter.convertToEntityAttribute(null));
    }

    @Test
    @DisplayName("Criptografia e Descriptografia - Deve criptografar e descriptografar restaurando valor original")
    void deveCriptografarEDescriptografar() {
        String cpfOriginal = "12345678901";

        String criptografado = cpfConverter.convertToDatabaseColumn(cpfOriginal);
        assertNotNull(criptografado);
        assertNotEquals(cpfOriginal, criptografado);

        String descriptografado = cpfConverter.convertToEntityAttribute(criptografado);
        assertEquals(cpfOriginal, descriptografado);
    }
}
