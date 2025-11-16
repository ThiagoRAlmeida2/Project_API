package kairos.residencia.controller;

import lombok.Data;

@Data
public class UpdatePerfilRequest {
    private String nome;
    private String curso;
    private String matricula;
    private String descricao;
    private String tags;

    private String cnpj;
}