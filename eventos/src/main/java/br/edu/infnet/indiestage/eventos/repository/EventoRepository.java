package br.edu.infnet.indiestage.eventos.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.infnet.indiestage.eventos.domain.Evento;
public interface EventoRepository extends JpaRepository<Evento,Long> {}