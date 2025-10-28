package kairos.residencia.repository;

import kairos.residencia.model.Aluno;
import kairos.residencia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    // Método necessário para buscar o aluno logado
    Aluno findByUsuario(Usuario usuario);
}