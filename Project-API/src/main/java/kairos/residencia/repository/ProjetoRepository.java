package kairos.residencia.repository;

import kairos.residencia.model.Empresa;
import kairos.residencia.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    List<Projeto> findByEmpresa(Empresa empresa);

    // Método para a rota pública: apenas projetos ATIVOS e carrega a empresa
    @Query("SELECT p FROM Projeto p JOIN FETCH p.empresa WHERE p.encerrado = false")
    List<Projeto> findAllActiveWithEmpresa();

    @Query("SELECT p FROM Projeto p JOIN FETCH p.empresa WHERE p.id = :id")
    Optional<Projeto> findByIdWithEmpresa(@Param("id") Long id);
}