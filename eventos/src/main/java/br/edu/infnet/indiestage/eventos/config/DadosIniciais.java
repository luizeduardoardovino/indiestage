package br.edu.infnet.indiestage.eventos.config;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import br.edu.infnet.indiestage.eventos.domain.*;
import br.edu.infnet.indiestage.eventos.repository.EventoRepository;
@Component
public class DadosIniciais implements CommandLineRunner {
    private final EventoRepository repository;
    public DadosIniciais(EventoRepository repository){this.repository=repository;}
    @Override @Transactional
    public void run(String... args){
        if(repository.count()==0) repository.saveAll(List.of(
            new Evento("Jazz no Rooftop","Apresentação de jazz independente","MUSICA",LocalDateTime.of(2026,11,10,20,0),1L,SituacaoEvento.AGENDADO),
            new Evento("Mostra Curta Independente","Sessão de curtas-metragens","CINEMA",LocalDateTime.of(2026,11,14,18,30),2L,SituacaoEvento.AGENDADO),
            new Evento("Workshop de Fotografia Urbana","Oficina prática de fotografia","WORKSHOP",LocalDateTime.of(2026,11,21,9,0),3L,SituacaoEvento.AGENDADO),
            new Evento("Festival IndieStage","Encontro cultural independente","FESTIVAL",LocalDateTime.of(2026,12,5,14,0),4L,SituacaoEvento.AGENDADO)));
    }
}