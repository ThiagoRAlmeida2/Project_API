package kairos.residencia.Dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateEventDto {

    @NotBlank(message = "O título é obrigatório")
    private String title;

    @NotBlank(message = "A descrição é obrigatória")
    private String description;

    @NotBlank(message = "A data é obrigatória")
    private String date;

    @NotBlank(message = "O local é obrigatório")
    private String location;

    @NotBlank(message = "A categoria é obrigatória")
    private String category;

    private String imageUrl;
}