package br.edu.infnet.indiestage.eventos.service;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import br.edu.infnet.indiestage.eventos.dto.*;
import br.edu.infnet.indiestage.eventos.domain.Evento;
import br.edu.infnet.indiestage.eventos.repository.EventoRepository;
@Service
@Transactional(readOnly=true)
public class EventoService {
    private final EventoRepository repository;
    public EventoService(EventoRepository repository){this.repository=repository;}
    public List<EventoResponse> listar(){return repository.findAll(Sort.by("id")).stream().map(this::resposta).toList();}
    public PaginaResponse<EventoResponse> paginar(int pagina,int tamanho){
        if(pagina<0||tamanho<1||tamanho>100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "pagina deve ser >= 0 e tamanho entre 1 e 100");
        return PaginaResponse.de(repository.findAll(PageRequest.of(pagina,tamanho,Sort.by("id"))).map(this::resposta));
    }
    public EventoResponse buscar(Long id){return resposta(encontrar(id));}
    @Transactional
    public EventoResponse criar(EventoRequest req){return resposta(repository.save(new Evento(req.nome().strip(),
            req.descricao().strip(),req.categoria().strip(),req.dataHora(),req.espacoId(),req.situacao())));}
    @Transactional
    public EventoResponse atualizar(Long id,EventoRequest req){
        Evento e=encontrar(id);e.atualizar(req.nome().strip(),req.descricao().strip(),req.categoria().strip(),
                req.dataHora(),req.espacoId(),req.situacao());return resposta(e);
    }
    @Transactional public void excluir(Long id){repository.delete(encontrar(id));}
    private Evento encontrar(Long id){return repository.findById(id).orElseThrow(()->new ResponseStatusException(
            HttpStatus.NOT_FOUND,"Evento não encontrado"));}
    private EventoResponse resposta(Evento e){return new EventoResponse(e.getId(),e.getNome(),e.getDescricao(),
            e.getCategoria(),e.getDataHora(),e.getEspacoId(),e.getSituacao());}
}