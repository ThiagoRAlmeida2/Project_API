package kairos.residencia.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjetoResponse(
        Long id,
        String nome,
        String descricao,
        LocalDateTime dataCriacao,
        String empresaNome,
        boolean encerrado,
        String tags,
        String regime,
        LocalDate dataInicio,
        LocalDate dataFim,
        long totalCandidatos,
        long aprovados
) {}