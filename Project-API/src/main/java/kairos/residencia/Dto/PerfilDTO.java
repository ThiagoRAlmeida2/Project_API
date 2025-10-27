package kairos.residencia.Dto;

import lombok.Data;

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
    }

    @Data
    public static class EmpresaDTO {
        private String nome;
        private String cnpj;
    }
}
