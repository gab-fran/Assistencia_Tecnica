package br.senai.backendassitencia.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
@Converter
public class CpfConverter implements AttributeConverter<String, String> {

    private final TextEncryptor encryptor;

    public CpfConverter(
            @Value("${app.criptografia.senha}") String senha,
            @Value("${app.criptografia.salt}") String salt) {
        this.encryptor = Encryptors.text(senha, salt);
    }

    @Override
    public String convertToDatabaseColumn(String cpfOriginal) {
        if (cpfOriginal == null) return null;
        return encryptor.encrypt(cpfOriginal);
    }

    @Override
    public String convertToEntityAttribute(String cpfDoBanco) {
        if (cpfDoBanco == null) return null;
        return encryptor.decrypt(cpfDoBanco);
    }
}