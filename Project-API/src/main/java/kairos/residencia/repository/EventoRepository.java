package kairos.residencia.repository;

import kairos.residencia.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
        List<Evento> findByEmpresaId(Long empresaId);
}