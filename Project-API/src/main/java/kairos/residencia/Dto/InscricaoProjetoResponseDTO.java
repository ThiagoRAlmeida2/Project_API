package kairos.residencia.Dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InscricaoProjetoResponseDTO {
    // Campos do Projeto
    private Long id;
    private String nome;
    private String descricao;
    private LocalDateTime dataCriacao;
    private String empresaNome;
    private boolean encerrado;
    private String tags;
    private String regime;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    // Campo CRÍTICO da Inscrição
    private String status;
}