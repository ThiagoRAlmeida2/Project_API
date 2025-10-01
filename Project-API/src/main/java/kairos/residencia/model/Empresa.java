package kairos.residencia.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name="empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cnpj;
    private String senha;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Projeto> projetos = new ArrayList<>();
}
