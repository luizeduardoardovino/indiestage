package br.edu.infnet.indiestage.espacos.controller;
import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.edu.infnet.indiestage.espacos.dto.*;
import br.edu.infnet.indiestage.espacos.service.EspacoService;

@RestController
@RequestMapping("/api/espacos")
public class EspacoController {
    private final EspacoService service;
    public EspacoController(EspacoService service) { this.service=service; }
    @GetMapping
    public List<EspacoResponse> listar() { return service.listar(); }
    @GetMapping("/paginados")
    public PaginaResponse<EspacoResponse> paginar(@RequestParam(defaultValue="0") int pagina,
            @RequestParam(defaultValue="10") int tamanho) { return service.paginar(pagina,tamanho); }
    @GetMapping("/{id}")
    public EspacoResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping
    public ResponseEntity<EspacoResponse> criar(@Valid @RequestBody EspacoRequest req) {
        EspacoResponse criado=service.criar(req);
        return ResponseEntity.created(URI.create("/api/espacos/"+criado.id())).body(criado);
    }
    @PutMapping("/{id}")
    public EspacoResponse atualizar(@PathVariable Long id,@Valid @RequestBody EspacoRequest req) {
        return service.atualizar(id,req);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}