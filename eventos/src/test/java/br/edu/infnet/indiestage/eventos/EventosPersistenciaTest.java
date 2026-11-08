package br.edu.infnet.indiestage.eventos;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import br.edu.infnet.indiestage.eventos.domain.Evento;
import br.edu.infnet.indiestage.eventos.repository.EventoRepository;
import static org.assertj.core.api.Assertions.assertThat;

class EventosPersistenciaTest {
    @TempDir
    Path diretorio;

    @Test
    void devePreservarRegistroESemearSemDuplicarAoReabrirBanco() {
        String url = "jdbc:h2:file:" + diretorio.resolve("eventos").toAbsolutePath()
                .toString().replace('\\', '/') + ";DB_CLOSE_ON_EXIT=FALSE";
        Long id;
        try (ConfigurableApplicationContext app = abrir(url)) {
            EventoRepository repository = app.getBean(EventoRepository.class);
            assertThat(repository.count()).isEqualTo(4);
            id = repository.saveAndFlush(new Evento("Evento extra", "Descrição", "MUSICA", java.time.LocalDateTime.of(2026,12,1,20,0), 1L, br.edu.infnet.indiestage.eventos.domain.SituacaoEvento.AGENDADO)).getId();
        }
        // Nova inicialização, mesmo arquivo: o registro extra deve sobreviver sem duplicar exemplos.
        try (ConfigurableApplicationContext app = abrir(url)) {
            EventoRepository repository = app.getBean(EventoRepository.class);
            assertThat(repository.count()).isEqualTo(5);
            Evento salvo = repository.findById(id).orElseThrow();
            assertThat(salvo.getNome()).isEqualTo("Evento extra");
            assertThat(salvo.getEspacoId()).isEqualTo(1L);
        }
    }

    private ConfigurableApplicationContext abrir(String url) {
        return new SpringApplicationBuilder(EventosApplication.class)
                .web(WebApplicationType.NONE)
                .run("--spring.datasource.url=" + url,
                        "--spring.jpa.hibernate.ddl-auto=update",
                        "--spring.main.banner-mode=off");
    }
}