package br.edu.infnet.indiestage.espacos.config;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import br.edu.infnet.indiestage.espacos.domain.Espaco;
import br.edu.infnet.indiestage.espacos.repository.EspacoRepository;

@Component
public class DadosIniciais implements CommandLineRunner {
    private final EspacoRepository repository;

    public DadosIniciais(EspacoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Carrega os exemplos somente quando a tabela estiver vazia.
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    new Espaco("Galpão 47", "Rio de Janeiro", 120, true),
                    new Espaco("Cinema Aurora", "Niterói", 80, true),
                    new Espaco("Casa Prisma", "Rio de Janeiro", 60, true),
                    new Espaco("Estação Norte", "Petrópolis", 200, true)));
        }
    }
}