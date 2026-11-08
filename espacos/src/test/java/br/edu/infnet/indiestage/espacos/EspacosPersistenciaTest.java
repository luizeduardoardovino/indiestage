package br.edu.infnet.indiestage.espacos;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import br.edu.infnet.indiestage.espacos.domain.Espaco;
import br.edu.infnet.indiestage.espacos.repository.EspacoRepository;
import static org.assertj.core.api.Assertions.assertThat;

class EspacosPersistenciaTest {
    @TempDir
    Path diretorio;

    @Test
    void devePreservarRegistroESemearSemDuplicarAoReabrirBanco() {
        String url = "jdbc:h2:file:" + diretorio.resolve("espacos").toAbsolutePath()
                .toString().replace('\\', '/') + ";DB_CLOSE_ON_EXIT=FALSE";
        Long id;
        try (ConfigurableApplicationContext app = abrir(url)) {
            EspacoRepository repository = app.getBean(EspacoRepository.class);
            assertThat(repository.count()).isEqualTo(4);
            id = repository.saveAndFlush(new Espaco("Espaço de teste", "Recife", 50, false)).getId();
        }
        // Nova inicialização, mesmo arquivo: o registro extra deve sobreviver sem duplicar exemplos.
        try (ConfigurableApplicationContext app = abrir(url)) {
            EspacoRepository repository = app.getBean(EspacoRepository.class);
            assertThat(repository.count()).isEqualTo(5);
            Espaco salvo = repository.findById(id).orElseThrow();
            assertThat(salvo.getNome()).isEqualTo("Espaço de teste");
            assertThat(salvo.isDisponivel()).isFalse();
        }
    }

    private ConfigurableApplicationContext abrir(String url) {
        return new SpringApplicationBuilder(EspacosApplication.class)
                .web(WebApplicationType.NONE)
                .run("--spring.datasource.url=" + url,
                        "--spring.jpa.hibernate.ddl-auto=update",
                        "--spring.main.banner-mode=off");
    }
}