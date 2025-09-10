package kairos.residencia.repository;

import kairos.residencia.model.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {
    boolean existsByProjeto_IdAndAluno_Id(Long projetoId, Long alunoId);
    List<Inscricao> findByAluno_Id(Long alunoId);
}
