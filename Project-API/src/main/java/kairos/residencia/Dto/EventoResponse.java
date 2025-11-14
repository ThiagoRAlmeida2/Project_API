package kairos.residencia.Dto;

import lombok.Data;

@Data
public class EventoResponse {
    private Long id;
    private String title;
    private String description;
    private String date;
    private String location;
    private String category;
    private String imageUrl;
    private boolean featured;
    private String empresaNome;
}