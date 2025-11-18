package kairos.residencia.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // 👈 NOVA IMPORTAÇÃO
import java.time.LocalDateTime; // 👈 NOVA IMPORTAÇÃO
import java.util.*;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String role; // "ROLE_ALUNO" ou "ROLE_EMPRESA" ou "ROLE_ADMIN"

    @CreationTimestamp
    @Column(updatable = false, nullable = true)
    private LocalDateTime dataCadastro;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Aluno aluno;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Empresa empresa;

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InscricaoEvento> inscricoesEventos;
}