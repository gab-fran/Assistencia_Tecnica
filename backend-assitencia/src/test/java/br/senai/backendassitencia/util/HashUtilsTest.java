package br.senai.backendassitencia.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashUtilsTest {

    @Test
    @DisplayName("gerarHmac - Deve gerar hash determinístico para mesma entrada e chave")
    void deveGerarHmacDeterministico() {
        String dado = "12345678901";
        String chave = "minha-chave-secreta";

        String hash1 = HashUtils.gerarHmac(dado, chave);
        String hash2 = HashUtils.gerarHmac(dado, chave);

        assertNotNull(hash1);
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("gerarHmac - Deve gerar hashes diferentes para dados diferentes")
    void deveGerarHashesDiferentes() {
        String chave = "minha-chave-secreta";

        String hash1 = HashUtils.gerarHmac("12345678901", chave);
        String hash2 = HashUtils.gerarHmac("12345678902", chave);

        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("gerarHmac - Deve lançar RuntimeException quando parâmetros forem inválidos")
    void deveLancarExcecaoQuandoChaveForNula() {
        assertThrows(RuntimeException.class, () -> HashUtils.gerarHmac("123", null));
    }
}
