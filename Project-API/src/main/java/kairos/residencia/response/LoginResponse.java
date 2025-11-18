package kairos.residencia.response;

import kairos.residencia.Dto.PerfilDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private PerfilDTO user;
}