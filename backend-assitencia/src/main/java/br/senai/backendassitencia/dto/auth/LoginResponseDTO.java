package br.senai.backendassitencia.dto.auth;

public record LoginResponseDTO(
        String token,
        String type
) {}