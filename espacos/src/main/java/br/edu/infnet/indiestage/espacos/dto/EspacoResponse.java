package br.edu.infnet.indiestage.espacos.dto;

public record EspacoResponse(
        Long id,
        String nome,
        String cidade,
        int capacidade,
        boolean disponivel) {
}