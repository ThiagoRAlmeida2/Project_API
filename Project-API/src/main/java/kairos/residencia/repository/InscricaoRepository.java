package kairos.residencia.repository;

import kairos.residencia.model.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional; // Importe este

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    // Método para verificar existência (já existente)
    boolean existsByProjeto_IdAndAluno_Id(Long projetoId, Long alunoId);

    // Método para buscar a entidade de inscrição (para deleção) 🚩 NOVO
    Optional<Inscricao> findByProjeto_IdAndAluno_Id(Long projetoId, Long alunoId);

    List<Inscricao> findByAluno_Id(Long alunoId);
}