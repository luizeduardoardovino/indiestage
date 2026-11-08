package br.edu.infnet.indiestage.eventos.service;
import org.springframework.stereotype.Service;
import br.edu.infnet.indiestage.eventos.client.EspacosClient;
import br.edu.infnet.indiestage.eventos.dto.EventoDetalhesResponse;
@Service
public class EventoDetalhesService {
    private final EventoService eventos;
    private final EspacosClient espacos;
    public EventoDetalhesService(EventoService eventos,EspacosClient espacos) {
        this.eventos=eventos; this.espacos=espacos;
    }
    public EventoDetalhesResponse buscar(Long id) {
        var evento=eventos.buscar(id);
        // Consulta remota fora da transação de leitura do banco de Eventos.
        return EventoDetalhesResponse.de(evento,espacos.consultar(evento.espacoId()));
    }
}