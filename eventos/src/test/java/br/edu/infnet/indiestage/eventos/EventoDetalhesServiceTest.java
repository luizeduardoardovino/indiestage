package br.edu.infnet.indiestage.eventos;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import br.edu.infnet.indiestage.eventos.client.*;
import br.edu.infnet.indiestage.eventos.dto.*;
import br.edu.infnet.indiestage.eventos.domain.SituacaoEvento;
import br.edu.infnet.indiestage.eventos.service.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class EventoDetalhesServiceTest {
    @Test void deveConsultarIdDoEspacoEManterIdDoEvento() {
        EventoService eventos=mock(EventoService.class);EspacosClient espacos=mock(EspacosClient.class);
        when(eventos.buscar(8L)).thenReturn(new EventoResponse(8L,"Show","Descrição","MUSICA",
                LocalDateTime.of(2026,12,1,20,0),3L,SituacaoEvento.AGENDADO));
        when(espacos.consultar(3L)).thenReturn(EspacoInfo.indisponivel(3L));
        var resultado=new EventoDetalhesService(eventos,espacos).buscar(8L);
        assertThat(resultado.id()).isEqualTo(8L);
        assertThat(resultado.espaco().id()).isEqualTo(3L);
        verify(espacos).consultar(3L);
    }
    @Test void naoDeveConsultarOutroServicoQuandoEventoNaoExiste() {
        EventoService eventos=mock(EventoService.class);EspacosClient espacos=mock(EspacosClient.class);
        when(eventos.buscar(8L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertThatThrownBy(()->new EventoDetalhesService(eventos,espacos).buscar(8L))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(espacos);
    }
}