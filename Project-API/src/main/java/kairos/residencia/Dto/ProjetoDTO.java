package kairos.residencia.Dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProjetoDTO {
    private String nome;
    private String descricao;

    private String tags;
    private String regime;
    private LocalDate dataInicio;
    private LocalDate dataFim;
}