package br.edu.infnet.indiestage.eventos.dto;
import java.time.LocalDateTime;
import br.edu.infnet.indiestage.eventos.domain.SituacaoEvento;
import br.edu.infnet.indiestage.eventos.client.EspacoInfo;
public record EventoDetalhesResponse(Long id,String nome,String descricao,String categoria,
        LocalDateTime dataHora,Long espacoId,SituacaoEvento situacao,EspacoInfo espaco) {
    public static EventoDetalhesResponse de(EventoResponse e,EspacoInfo espaco) {
        return new EventoDetalhesResponse(e.id(),e.nome(),e.descricao(),e.categoria(),e.dataHora(),
                e.espacoId(),e.situacao(),espaco);
    }
}