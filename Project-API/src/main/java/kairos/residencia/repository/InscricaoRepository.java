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

    @Query("SELECT i FROM Inscricao i JOIN FETCH i.projeto p JOIN FETCH p.empresa e WHERE i.aluno.id = :alunoId")
    List<Inscricao> findByAluno_Id(@Param("alunoId") Long alunoId);

    @Query("SELECT i FROM Inscricao i JOIN FETCH i.projeto p JOIN FETCH i.aluno a JOIN FETCH p.empresa e WHERE p.empresa.id = :empresaId")
    List<Inscricao> findByProjetoEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(i) FROM Inscricao i WHERE i.projeto.id = :projetoId")
    long countByProjetoId(@Param("projetoId") Long projetoId);

    @Query("SELECT COUNT(i) FROM Inscricao i WHERE i.projeto.id = :projetoId AND i.status = 'APROVADO'")
    long countAprovadosByProjetoId(@Param("projetoId") Long projetoId);
}