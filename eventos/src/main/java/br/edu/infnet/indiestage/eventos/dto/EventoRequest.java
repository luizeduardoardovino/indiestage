package br.edu.infnet.indiestage.eventos.dto;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
import br.edu.infnet.indiestage.eventos.domain.SituacaoEvento;
public record EventoRequest(
        @NotBlank @Size(max=255) String nome,
        @NotBlank @Size(max=2000) String descricao,
        @NotBlank @Size(max=100) String categoria,
        @NotNull LocalDateTime dataHora,
        @NotNull @Positive Long espacoId,
        @NotNull SituacaoEvento situacao) {}