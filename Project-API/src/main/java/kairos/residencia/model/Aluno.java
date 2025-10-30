package kairos.residencia.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name="aluno")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aluno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String curso;
    @Column(unique = true)
    private String matricula;

    // 🚩 NOVOS CAMPOS
    @Column(length = 1000)
    private String descricao;
    private String tags; // Tags como string separada por vírgulas

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscricao> inscricoes = new ArrayList<>();
}