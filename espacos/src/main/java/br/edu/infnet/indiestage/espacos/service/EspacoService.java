package br.edu.infnet.indiestage.espacos.service;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import br.edu.infnet.indiestage.espacos.dto.*;
import br.edu.infnet.indiestage.espacos.domain.Espaco;
import br.edu.infnet.indiestage.espacos.repository.EspacoRepository;

@Service
@Transactional(readOnly=true)
public class EspacoService {
    private final EspacoRepository repository;
    public EspacoService(EspacoRepository repository) { this.repository=repository; }
    public List<EspacoResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(this::resposta).toList();
    }
    public PaginaResponse<EspacoResponse> paginar(int pagina, int tamanho) {
        if (pagina<0 || tamanho<1 || tamanho>100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pagina deve ser >= 0 e tamanho entre 1 e 100");
        }
        return PaginaResponse.de(repository.findAll(PageRequest.of(pagina,tamanho,Sort.by("id")))
                .map(this::resposta));
    }
    public EspacoResponse buscar(Long id) { return resposta(encontrar(id)); }
    @Transactional
    public EspacoResponse criar(EspacoRequest req) {
        return resposta(repository.save(new Espaco(req.nome().strip(),req.cidade().strip(),
                req.capacidade(),req.disponivel())));
    }
    @Transactional
    public EspacoResponse atualizar(Long id,EspacoRequest req) {
        Espaco espaco=encontrar(id);
        espaco.atualizar(req.nome().strip(),req.cidade().strip(),req.capacidade(),req.disponivel());
        return resposta(espaco);
    }
    @Transactional
    public void excluir(Long id) { repository.delete(encontrar(id)); }
    private Espaco encontrar(Long id) {
        return repository.findById(id).orElseThrow(()->new ResponseStatusException(
                HttpStatus.NOT_FOUND,"Espaço não encontrado"));
    }
    private EspacoResponse resposta(Espaco e) {
        return new EspacoResponse(e.getId(),e.getNome(),e.getCidade(),e.getCapacidade(),e.isDisponivel());
    }
}