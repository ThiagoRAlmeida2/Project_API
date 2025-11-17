package kairos.residencia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kairos.residencia.Dto.CreateEventDto;
import kairos.residencia.Dto.EventoResponse;
import kairos.residencia.model.*;
import kairos.residencia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoRepository eventoRepo;
    private final UsuarioRepository usuarioRepo;
    private final Cloudinary cloudinary;
    private final ObjectMapper objectMapper;
    private final EmpresaRepository empresaRepo;
    private final AlunoRepository alunoRepo;
    private final InscricaoEventoRepository inscricaoEventoRepo;

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

            Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
            if (!"ROLE_EMPRESA".equals(usuario.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas Empresas podem criar eventos.");
            }
            Empresa empresa = empresaRepo.findByUsuario(usuario);

            if (empresa == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário empresa não associado a um registro de empresa.");
            }

            Map uploadResult;
            if (file != null && !file.isEmpty()) {
                uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            } else {
                throw new IOException("Arquivo de imagem é obrigatório.");
            }

            String imageUrl = uploadResult.get("secure_url").toString();
            Evento novoEvento = new Evento();
            novoEvento.setTitle(req.getTitle());
            novoEvento.setDescription(req.getDescription());
            novoEvento.setDate(req.getDate());
            novoEvento.setLocation(req.getLocation());
            novoEvento.setCategory(req.getCategory());
            novoEvento.setImageUrl(imageUrl);
            novoEvento.setEmpresa(empresa);
            Evento eventoSalvo = eventoRepo.save(novoEvento);

            EventoResponse responseDto = converterParaResponse(eventoSalvo);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao processar o arquivo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno: " + e.getMessage());
        }
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


    @PostMapping("/{id}/inscrever")
    public ResponseEntity<?> inscreverEmEvento(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));

        if (!"ROLE_ALUNO".equals(usuario.getRole()) || usuario.getAluno() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas Alunos podem se inscrever.");
        }
        Aluno aluno = usuario.getAluno();

        Evento evento = eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado."));

        Optional<InscricaoEvento> inscricaoExistente = inscricaoEventoRepo.findByAlunoIdAndEventoId(usuario.getId(), evento.getId());
        if (inscricaoExistente.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Você já está inscrito neste evento.");
        }

        InscricaoEvento novaInscricao = new InscricaoEvento();
        novaInscricao.setAluno(usuario); // [CORREÇÃO Bônus] Usando 'usuario' diretamente
        novaInscricao.setEvento(evento);

        inscricaoEventoRepo.save(novaInscricao);

        return ResponseEntity.status(HttpStatus.CREATED).body("Inscrição realizada com sucesso!");
    }

    @GetMapping("/minhas-inscricoes")
    public ResponseEntity<List<EventoResponse>> listarMinhasInscricoes(
            @AuthenticationPrincipal User user
    ) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));

        if (!"ROLE_ALUNO".equals(usuario.getRole()) || usuario.getAluno() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(List.of());
        }

        List<InscricaoEvento> inscricoes = inscricaoEventoRepo.findByAlunoId(usuario.getId());

        List<EventoResponse> eventosInscritos = inscricoes.stream()
                .map(InscricaoEvento::getEvento)
                .map(this::converterParaResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventosInscritos);
    }

    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarInscricaoEvento(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));

        if (!"ROLE_ALUNO".equals(usuario.getRole()) || usuario.getAluno() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas Alunos podem cancelar inscrições.");
        }

        InscricaoEvento inscricao = inscricaoEventoRepo.findByAlunoIdAndEventoId(usuario.getId(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscrição não encontrada."));

        inscricaoEventoRepo.delete(inscricao);

        return ResponseEntity.ok("Inscrição cancelada com sucesso.");
    }
}