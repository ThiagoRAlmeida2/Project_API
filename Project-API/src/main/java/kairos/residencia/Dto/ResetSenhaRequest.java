package kairos.residencia.Dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetSenhaRequest {
    private String email;
    private String novaSenha;
}
