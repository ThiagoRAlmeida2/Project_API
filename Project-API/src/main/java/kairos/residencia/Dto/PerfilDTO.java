package kairos.residencia.Dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PerfilDTO {
    private String email;
    private String role;
    private AlunoDTO aluno;
    private EmpresaDTO empresa;

    @Data
    public static class AlunoDTO {
        private String nome;
        private String curso;
        private String matricula;
        // 🚩 NOVOS CAMPOS DO ALUNO
        private String descricao;
        private String tags;
        private List<ProjetoParticipadoDTO> projetosParticipados;
    }

    @Data
    public static class EmpresaDTO {
        private String nome;
        private String cnpj;
    }

    @Data
    public static class ProjetoParticipadoDTO {
        private Long id;
        private String nome;
        private String empresaNome; // 💡 Manter no DTO, mas ocultar no Frontend
        private LocalDate dataInicio;

        // 🚩 NOVOS CAMPOS ADICIONADOS
        private String descricao;
        private String tags;
        private String regime;
        private LocalDate dataFim;
    }
}