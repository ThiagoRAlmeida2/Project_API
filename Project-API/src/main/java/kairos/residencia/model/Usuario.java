package kairos.residencia.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
        private String senha; // já criptografada

        @Column(nullable = false)
        private String role; // "ROLE_ALUNO" ou "ROLE_EMPRESA" ou "ROLE_ADMIN"

        // perfil opcional
        @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
        private Aluno aluno;

        @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
        private Empresa empresa;

        @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<InscricaoEvento> inscricoesEventos;
    }