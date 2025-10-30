package kairos.residencia.controller;

import lombok.Data;

@Data
public class UpdatePerfilRequest {
    // Campos necessários para a atualização do Aluno
    private String nome;
    private String curso;
    private String matricula;
    private String descricao; // 🚩 NOVO
    private String tags;      // 🚩 NOVO

    // Campos necessários para a atualização da Empresa (devem ser sobrepostos se a role for Empresa)
    private String cnpj;
}