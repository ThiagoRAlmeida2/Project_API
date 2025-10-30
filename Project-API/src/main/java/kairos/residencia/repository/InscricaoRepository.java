package kairos.residencia.repository;

import kairos.residencia.model.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    boolean existsByProjeto_IdAndAluno_Id(Long projetoId, Long alunoId);
    Optional<Inscricao> findByProjeto_IdAndAluno_Id(Long projetoId, Long alunoId);

    // 🚩 CORREÇÃO CRÍTICA: Faz JOIN FETCH do Projeto E da Empresa do Projeto
    @Query("SELECT i FROM Inscricao i JOIN FETCH i.projeto p JOIN FETCH p.empresa e WHERE i.aluno.id = :alunoId")
    List<Inscricao> findByAluno_Id(@Param("alunoId") Long alunoId);
}