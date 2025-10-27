package kairos.residencia.repository;

import kairos.residencia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("select u from Usuario u " +
            "left join fetch u.aluno " +
            "left join fetch u.empresa " +
            "where u.email = :email")
    Optional<Usuario> findByEmailWithProfile(@Param("email") String email);

    Optional<Usuario> findByEmail(String email);
}
