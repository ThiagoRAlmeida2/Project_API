package kairos.residencia.controller;

import com.fasterxml.jackson.databind.ObjectMapper; // 👈 IMPORTE
import kairos.residencia.Dto.CreateEventDto;
import kairos.residencia.Dto.EventoResponse;
import kairos.residencia.model.Empresa;
import kairos.residencia.model.Evento;
import kairos.residencia.model.Usuario;
import kairos.residencia.repository.EventoRepository;
import kairos.residencia.repository.UsuarioRepository;
import kairos.residencia.service.FileStorageService; // 👈 IMPORTE
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // 👈 IMPORTE
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 👈 IMPORTE
import org.springframework.web.server.ResponseStatusException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoRepository eventoRepo;
    private final UsuarioRepository usuarioRepo;
    private final Cloudinary cloudinary;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_IMAGE_URL = "/assets/IMG/Conferencia de tecnologia.jpg";

    @GetMapping
    public ResponseEntity<List<EventoResponse>> listarEventos(
            @AuthenticationPrincipal User user
    ) {
        List<Evento> eventos;

        if (user != null) {
            Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

            if ("ROLE_EMPRESA".equals(usuario.getRole()) && usuario.getEmpresa() != null) {
                Long empresaId = usuario.getEmpresa().getId();
                eventos = eventoRepo.findByEmpresaId(empresaId);
            } else {
                eventos = eventoRepo.findAll();
            }
        } else {
            eventos = eventoRepo.findAll();
        }

        List<EventoResponse> responseList = eventos.stream()
                .map(this::converterParaResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }


    @PostMapping(value = "/criar", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> criarEvento(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file,
            @RequestParam("eventData") String eventDataJson
    ) {
        try {
            CreateEventDto req = objectMapper.readValue(eventDataJson, CreateEventDto.class);

            // ... (lógica de verificação de usuário fica igual) ...
            Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

            if (!"ROLE_EMPRESA".equals(usuario.getRole()) || usuario.getEmpresa() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas Empresas podem criar eventos.");
            }
            Empresa empresa = usuario.getEmpresa();

            Map uploadResult;
            if (file != null && !file.isEmpty()) {
                uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            } else {
                throw new IOException("Arquivo de imagem é obrigatório.");
            }

            String imageUrl = uploadResult.get("secure_url").toString();

            Evento novoEvento = new Evento();
            novoEvento.setTitle(req.getTitle());
            novoEvento.setImageUrl(imageUrl);
            novoEvento.setEmpresa(empresa);

            Evento eventoSalvo = eventoRepo.save(novoEvento);
            EventoResponse responseDto = converterParaResponse(eventoSalvo);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao processar o arquivo: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> encerrarEvento(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        // ... (código inalterado) ...
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

    private EventoResponse converterParaResponse(Evento evento) {
        EventoResponse dto = new EventoResponse();
        dto.setId(evento.getId());
        dto.setTitle(evento.getTitle());
        dto.setDescription(evento.getDescription());
        dto.setDate(evento.getDate());
        dto.setLocation(evento.getLocation());
        dto.setCategory(evento.getCategory());
        dto.setImageUrl(evento.getImageUrl());
        dto.setFeatured(evento.isFeatured());
        if (evento.getEmpresa() != null) {
            dto.setEmpresaNome(evento.getEmpresa().getNome());
        }
        return dto;
    }
}