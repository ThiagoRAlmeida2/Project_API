package kairos.residencia.controller;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String senha;
    private String role; // "ALUNO" ou "EMPRESA"
    private String nome; // aluno ou empresa
    private String curso; // opcional para aluno
    private String matricula; // opcional para aluno
    private String cnpj; // opcional para empresa
}
