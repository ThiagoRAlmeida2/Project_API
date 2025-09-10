package kairos.residencia.repository;

import kairos.residencia.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
    List<Projeto> findByEmpresa_Id(Long empresaId);
}
