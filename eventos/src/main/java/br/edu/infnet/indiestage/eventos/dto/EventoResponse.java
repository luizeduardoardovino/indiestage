package br.edu.infnet.indiestage.eventos.dto;
import java.time.LocalDateTime;
import br.edu.infnet.indiestage.eventos.domain.SituacaoEvento;
public record EventoResponse(Long id,String nome,String descricao,String categoria,
        LocalDateTime dataHora,Long espacoId,SituacaoEvento situacao) {}