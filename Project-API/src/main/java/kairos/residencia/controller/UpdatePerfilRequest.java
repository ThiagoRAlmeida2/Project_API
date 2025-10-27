package kairos.residencia.controller;

import lombok.Data;

@Data
public class UpdatePerfilRequest {
    private String nome;
    private String curso;      // apenas para aluno
    private String matricula;  // apenas para aluno
    private String cnpj;       // apenas para empresa
}
