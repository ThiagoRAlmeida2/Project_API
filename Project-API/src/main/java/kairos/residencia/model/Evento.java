package kairos.residencia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "evento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String date; // Manter como String por enquanto, conforme o front (Ex: "15 Jan")

    @Column(nullable = false)
    private String location; // Online ou local físico

    @Column(nullable = false)
    private String category;

    private String imageUrl; // URL ou path para a imagem

    private boolean featured = false; // Se deve ser destacado

    // Relacionamento: Uma Empresa pode criar muitos Eventos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InscricaoEvento> inscricoes;
}