package kairos.residencia.Dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CandidatoResponse {
    // Dados do Aluno (Para exibição na tabela e link para o perfil)
    private Long alunoId;
    private String alunoNome;
    private String alunoMatricula;

    // Dados do Projeto
    private Long projetoId;
    private String projetoNome;

    // Detalhes da Inscrição
    private LocalDateTime dataInscricao;
    private Long inscricaoId; // ID da inscrição, CRÍTICO para as ações
    private String status; // Novo: Adicione um campo de status se for salvar a decisão (Aprovado/Pendente)
}

