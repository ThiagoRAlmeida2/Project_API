package kairos.residencia.Dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CandidatoResponse {
    // Dados do Aluno
    private Long alunoId;
    private String alunoNome;
    private String alunoMatricula;

    // Dados do Projeto
    private Long projetoId;
    private String projetoNome;

    // Detalhes da Inscrição
    private LocalDateTime dataInscricao;
    private Long inscricaoId;
    private String status;
}

