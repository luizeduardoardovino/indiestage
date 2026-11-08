package br.edu.infnet.indiestage.eventos.controller;
import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.edu.infnet.indiestage.eventos.dto.*;
import br.edu.infnet.indiestage.eventos.service.EventoService;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {
    private final EventoService service;
    private final br.edu.infnet.indiestage.eventos.service.EventoDetalhesService detalhes;
    public EventoController(EventoService service, br.edu.infnet.indiestage.eventos.service.EventoDetalhesService detalhes) { this.service=service; this.detalhes=detalhes; }
    @GetMapping("/{id}/detalhes")
    public EventoDetalhesResponse detalhes(@PathVariable Long id) { return detalhes.buscar(id); }
    @GetMapping
    public List<EventoResponse> listar() { return service.listar(); }
    @GetMapping("/paginados")
    public PaginaResponse<EventoResponse> paginar(@RequestParam(defaultValue="0") int pagina,
            @RequestParam(defaultValue="10") int tamanho) { return service.paginar(pagina,tamanho); }
    @GetMapping("/{id}")
    public EventoResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping
    public ResponseEntity<EventoResponse> criar(@Valid @RequestBody EventoRequest req) {
        EventoResponse criado=service.criar(req);
        return ResponseEntity.created(URI.create("/api/eventos/"+criado.id())).body(criado);
    }
    @PutMapping("/{id}")
    public EventoResponse atualizar(@PathVariable Long id,@Valid @RequestBody EventoRequest req) {
        return service.atualizar(id,req);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}