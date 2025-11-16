package kairos.residencia.controller;

import com.cloudinary.Cloudinary; // 👈 IMPORTE
import com.cloudinary.utils.ObjectUtils; // 👈 IMPORTE
import kairos.residencia.Dto.CandidatoResponse;
import kairos.residencia.Dto.PerfilDTO;
import kairos.residencia.model.Empresa;
import kairos.residencia.model.Inscricao;
import kairos.residencia.model.Projeto;
import kairos.residencia.model.Usuario;
import kairos.residencia.repository.AlunoRepository;
import kairos.residencia.repository.InscricaoRepository;
import kairos.residencia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // 👈 IMPORTE
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 👈 IMPORTE

import java.io.IOException; // 👈 IMPORTE
import java.util.List;
import java.util.Map; // 👈 IMPORTE

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioRepository usuarioRepo;
    private final InscricaoRepository inscricaoRepo;
    private final AlunoRepository alunoRepo;
    private final Cloudinary cloudinary; // 👈 INJEÇÃO DO CLOUDINARY

    private PerfilDTO buildPerfilDTO(Usuario u) {
        PerfilDTO dto = new PerfilDTO();
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());

        if (u.getAluno() != null) {
            PerfilDTO.AlunoDTO a = new PerfilDTO.AlunoDTO();
            a.setNome(u.getAluno().getNome());
            a.setCurso(u.getAluno().getCurso());
            a.setMatricula(u.getAluno().getMatricula());
            a.setDescricao(u.getAluno().getDescricao());
            a.setTags(u.getAluno().getTags());
            a.setFotoUrl(u.getAluno().getFotoUrl()); // 👈 MAPEIA A FOTO

            // Mapeia Projetos Participados
            List<Inscricao> inscricoes = inscricaoRepo.findByAluno_Id(u.getAluno().getId());
            List<PerfilDTO.ProjetoParticipadoDTO> projetos = inscricoes.stream()
                    .filter(i -> i.getProjeto() != null)
                    .map(i -> {
                        Projeto p = i.getProjeto();
                        PerfilDTO.ProjetoParticipadoDTO pDto = new PerfilDTO.ProjetoParticipadoDTO();
                        pDto.setId(p.getId());
                        pDto.setNome(p.getNome());
                        pDto.setEmpresaNome(p.getEmpresa().getNome());
                        pDto.setDataInicio(p.getDataInicio());
                        pDto.setDescricao(p.getDescricao());
                        pDto.setTags(p.getTags());
                        pDto.setRegime(p.getRegime());
                        pDto.setDataFim(p.getDataFim());
                        return pDto;
                    })
                    .toList();
            a.setProjetosParticipados(projetos);
            dto.setAluno(a);
        }

        if (u.getEmpresa() != null) {
            PerfilDTO.EmpresaDTO e = new PerfilDTO.EmpresaDTO();
            e.setNome(u.getEmpresa().getNome());
            e.setCnpj(u.getEmpresa().getCnpj());
            e.setFotoUrl(u.getEmpresa().getFotoUrl()); // 👈 MAPEIA A FOTO
            dto.setEmpresa(e);
        }
        return dto;
    }

    // -----------------------------------------------------
    // 🔹 ENDPOINTS DE PERFIL
    // -----------------------------------------------------

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user){
        var u = usuarioRepo.findByEmail(user.getUsername()).orElse(null);
        if(u==null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(buildPerfilDTO(u));
    }

    // 👇 NOVO ENDPOINT DE UPLOAD DE FOTO 👇
    @PostMapping(value = "/me/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFotoPerfil(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo de imagem é obrigatório.");
        }

        try {
            var u = usuarioRepo.findByEmail(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // 1. Upload para o Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = uploadResult.get("secure_url").toString();

            // 2. Salva a URL na entidade correta
            if ("ROLE_ALUNO".equals(u.getRole()) && u.getAluno() != null) {
                u.getAluno().setFotoUrl(url);
            } else if ("ROLE_EMPRESA".equals(u.getRole()) && u.getEmpresa() != null) {
                u.getEmpresa().setFotoUrl(url);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Perfil de aluno ou empresa não encontrado.");
            }

            usuarioRepo.save(u);

            // Retorna a URL nova
            return ResponseEntity.ok(Map.of("url", url));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao fazer upload da imagem: " + e.getMessage());
        }
    }
    // 👆 FIM DO NOVO ENDPOINT 👆


    @PutMapping("/me")
    public ResponseEntity<?> atualizarPerfil(
            @AuthenticationPrincipal User user,
            @RequestBody UpdatePerfilRequest req
    ) {
        var u = usuarioRepo.findByEmail(user.getUsername()).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();

        if ("ROLE_ALUNO".equals(u.getRole()) && u.getAluno() != null) {
            u.getAluno().setNome(req.getNome());
            u.getAluno().setCurso(req.getCurso());
            u.getAluno().setMatricula(req.getMatricula());
            u.getAluno().setDescricao(req.getDescricao());
            u.getAluno().setTags(req.getTags());

        } else if ("ROLE_EMPRESA".equals(u.getRole()) && u.getEmpresa() != null) {
            u.getEmpresa().setNome(req.getNome());
            u.getEmpresa().setCnpj(req.getCnpj());
        }

        usuarioRepo.save(u);

        return me(user);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMe(@AuthenticationPrincipal User user){
        var opt = usuarioRepo.findByEmail(user.getUsername());
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        usuarioRepo.delete(opt.get());
        return ResponseEntity.ok("Perfil e dados associados deletados");
    }

    // ... (Os métodos do Dashboard /dashboard/candidatos, aprovar, rejeitar, etc, continuam iguais abaixo)
    // Mantenha o restante do arquivo como estava...
    @GetMapping("/dashboard/candidatos")
    public ResponseEntity<List<CandidatoResponse>> listarCandidatosDashboard(@AuthenticationPrincipal User user) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            return ResponseEntity.status(403).body(null);
        }

        List<Inscricao> inscricoes = inscricaoRepo.findByProjetoEmpresaId(empresa.getId());

        List<CandidatoResponse> candidatos = inscricoes.stream()
                .filter(i -> i.getProjeto() != null && i.getAluno() != null)
                .map(i -> {
                    CandidatoResponse dto = new CandidatoResponse();
                    dto.setInscricaoId(i.getId());
                    dto.setAlunoId(i.getAluno().getId());
                    dto.setAlunoNome(i.getAluno().getNome());
                    dto.setAlunoMatricula(i.getAluno().getMatricula());
                    dto.setProjetoId(i.getProjeto().getId());
                    dto.setProjetoNome(i.getProjeto().getNome());
                    dto.setDataInscricao(i.getDataInscricao());
                    dto.setStatus(i.getStatus());
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(candidatos);
    }

    @GetMapping("/aluno/{alunoId}/perfil-detalhado")
    public ResponseEntity<?> getPerfilDetalhadoAluno(
            @AuthenticationPrincipal User user,
            @PathVariable Long alunoId
    ) {
        if (!"ROLE_EMPRESA".equals(user.getAuthorities().iterator().next().getAuthority())) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas empresas podem visualizar perfis detalhados.");
        }
        var alunoOpt = alunoRepo.findById(alunoId);
        if (alunoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Usuario alunoUsuario = alunoOpt.get().getUsuario();
        return ResponseEntity.ok(buildPerfilDTO(alunoUsuario));
    }

    @PostMapping("/inscricao/{inscricaoId}/aprovar")
    public ResponseEntity<?> aprovarCandidato(
            @AuthenticationPrincipal User user,
            @PathVariable Long inscricaoId
    ) {
        return atualizarStatusInscricao(user, inscricaoId, "APROVADO");
    }

    @PostMapping("/inscricao/{inscricaoId}/rejeitar")
    public ResponseEntity<?> declinarCandidato(
            @AuthenticationPrincipal User user,
            @PathVariable Long inscricaoId
    ) {
        return atualizarStatusInscricao(user, inscricaoId, "REJEITADO");
    }

    private ResponseEntity<?> atualizarStatusInscricao(User user, Long inscricaoId, String novoStatus) {
        var usuarioOpt = usuarioRepo.findByEmail(user.getUsername());
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getEmpresa() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
        }
        Empresa empresaLogada = usuarioOpt.get().getEmpresa();
        Inscricao inscricao = inscricaoRepo.findById(inscricaoId)
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada."));

        if (inscricao.getProjeto() == null || inscricao.getProjeto().getEmpresa() == null ||
                !inscricao.getProjeto().getEmpresa().getId().equals(empresaLogada.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não é o proprietário deste projeto ou o projeto é inválido.");
        }
        inscricao.setStatus(novoStatus);
        inscricaoRepo.save(inscricao);
        return ResponseEntity.ok(novoStatus + " com sucesso.");
    }
}