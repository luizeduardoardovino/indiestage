package br.edu.infnet.indiestage.espacos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.infnet.indiestage.espacos.domain.Espaco;

public interface EspacoRepository extends JpaRepository<Espaco, Long> {
}