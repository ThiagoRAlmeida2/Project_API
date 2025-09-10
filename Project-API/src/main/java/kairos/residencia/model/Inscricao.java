package kairos.residencia.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="inscricao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscricao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="projeto_id")
    private Projeto projeto;

    @ManyToOne
    @JoinColumn(name="aluno_id")
    private Aluno aluno;

    private LocalDateTime dataInscricao = LocalDateTime.now();
    private String papel; // ex: "participante"
}
