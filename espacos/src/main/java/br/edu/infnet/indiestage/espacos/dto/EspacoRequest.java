package br.edu.infnet.indiestage.espacos.dto;
import jakarta.validation.constraints.*;
public record EspacoRequest(
        @NotBlank @Size(max=255) String nome,
        @NotBlank @Size(max=255) String cidade,
        @NotNull @Positive Integer capacidade,
        @NotNull Boolean disponivel) {}