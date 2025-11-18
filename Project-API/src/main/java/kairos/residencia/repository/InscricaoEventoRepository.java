package kairos.residencia.repository;

import kairos.residencia.model.InscricaoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InscricaoEventoRepository extends JpaRepository<InscricaoEvento, Long> {
    List<InscricaoEvento> findByAlunoId(Long alunoId);
    Optional<InscricaoEvento> findByAlunoIdAndEventoId(Long alunoId, Long eventoId);
}