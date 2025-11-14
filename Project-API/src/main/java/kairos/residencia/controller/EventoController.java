package kairos.residencia.controller;

import kairos.residencia.Dto.CreateEventDto;
import kairos.residencia.Dto.EventoResponse;
import kairos.residencia.model.Empresa;
import kairos.residencia.model.Evento;
import kairos.residencia.model.Usuario;
import kairos.residencia.repository.EventoRepository;
import kairos.residencia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoRepository eventoRepo;
    private final UsuarioRepository usuarioRepo;

    private static final String DEFAULT_IMAGE_URL = "/assets/IMG/Conferencia de tecnologia.jpg";

    @PostMapping("/criar")
    public ResponseEntity<?> criarEvento(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateEventDto req
    ) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!"ROLE_EMPRESA".equals(usuario.getRole()) || usuario.getEmpresa() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas Empresas podem criar eventos.");
        }
        Empresa empresa = usuario.getEmpresa();

        Evento novoEvento = new Evento();
        novoEvento.setTitle(req.getTitle());
        novoEvento.setDescription(req.getDescription());
        novoEvento.setDate(req.getDate());
        novoEvento.setLocation(req.getLocation());
        novoEvento.setCategory(req.getCategory());

        String finalImageUrl = req.getImageUrl() != null && !req.getImageUrl().isEmpty()
                ? req.getImageUrl()
                : DEFAULT_IMAGE_URL;
        novoEvento.setImageUrl(finalImageUrl);

        novoEvento.setEmpresa(empresa);
        novoEvento.setFeatured(false);

        Evento eventoSalvo = eventoRepo.save(novoEvento);

        EventoResponse responseDto = new EventoResponse();
        responseDto.setId(eventoSalvo.getId());
        responseDto.setTitle(eventoSalvo.getTitle());
        responseDto.setDescription(eventoSalvo.getDescription());
        responseDto.setDate(eventoSalvo.getDate());
        responseDto.setLocation(eventoSalvo.getLocation());
        responseDto.setCategory(eventoSalvo.getCategory());
        responseDto.setImageUrl(eventoSalvo.getImageUrl());
        responseDto.setFeatured(eventoSalvo.isFeatured());
        responseDto.setEmpresaNome(eventoSalvo.getEmpresa().getNome());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> encerrarEvento(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!"ROLE_EMPRESA".equals(usuario.getRole()) || usuario.getEmpresa() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado. Apenas Empresas podem encerrar eventos.");
        }

        Empresa empresaLogada = usuario.getEmpresa();

        Evento evento = eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado."));

        if (!evento.getEmpresa().getId().equals(empresaLogada.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado. Você não é o criador deste evento.");
        }

        eventoRepo.delete(evento);

        return ResponseEntity.ok("Evento encerrado e excluído com sucesso.");
    }
}