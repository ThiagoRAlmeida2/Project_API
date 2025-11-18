package kairos.residencia.Dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDate;

@Data
public class PerfilDTO {
    private String email;
    private String role;
    private AlunoDTO aluno;
    private EmpresaDTO empresa;

    private LocalDateTime dataCadastro;

    @Data
    public static class AlunoDTO {
        private String nome;
        private String curso;
        private String matricula;
        private String descricao;
        private String tags;
        private String fotoUrl;
        private List<ProjetoParticipadoDTO> projetosParticipados;
    }

    @Data
    public static class EmpresaDTO {
        private String nome;
        private String cnpj;
        private String fotoUrl;
    }

    @Data
    public static class ProjetoParticipadoDTO {
        private Long id;
        private String nome;
        private String empresaNome;
        private LocalDate dataInicio;
        private String descricao;
        private String tags;
        private String regime;
        private LocalDate dataFim;
    }
}