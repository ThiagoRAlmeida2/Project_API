package kairos.residencia.repository;

import kairos.residencia.model.Empresa;
import kairos.residencia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Empresa findByUsuario(Usuario usuario);
}
