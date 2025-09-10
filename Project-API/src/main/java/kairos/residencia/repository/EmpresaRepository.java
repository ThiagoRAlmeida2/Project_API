package kairos.residencia.repository;

import kairos.residencia.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByUsuario_Id(Long usuarioId);
}
