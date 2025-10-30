package kairos.residencia.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioRepository usuarioRepo;
    private final InscricaoRepository inscricaoRepo;
    private final AlunoRepository alunoRepo;

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
            dto.setEmpresa(e);
        }
        return dto;
    }

    // -----------------------------------------------------
    // 🔹 ENDPOINTS DE PERFIL (GET, PUT, DELETE)
    // -----------------------------------------------------

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user){
        var u = usuarioRepo.findByEmail(user.getUsername()).orElse(null);
        if(u==null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(buildPerfilDTO(u));
    }

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

    // -----------------------------------------------------
    // 🔹 ENDPOINTS DE DASHBOARD (Apenas Empresa)
    // -----------------------------------------------------

    // 🔹 1. LISTA TODOS OS CANDIDATOS PARA A EMPRESA LOGADA
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
                    dto.setInscricaoId(i.getId()); // ID da Inscrição (para ações)
                    dto.setAlunoId(i.getAluno().getId());
                    dto.setAlunoNome(i.getAluno().getNome());
                    dto.setAlunoMatricula(i.getAluno().getMatricula());
                    dto.setProjetoId(i.getProjeto().getId());
                    dto.setProjetoNome(i.getProjeto().getNome());
                    dto.setDataInscricao(i.getDataInscricao());
                    dto.setStatus(i.getStatus()); // Status (PENDENTE, APROVADO, REJEITADO)
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(candidatos);
    }

    // 🔹 2. RETORNA O PERFIL DETALHADO DE UM ALUNO (Para o Modal)
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

        // Reutiliza a função buildPerfilDTO para enviar o perfil completo (incluindo projetos participados)
        return ResponseEntity.ok(buildPerfilDTO(alunoUsuario));
    }

    // 🔹 3. APROVAR CANDIDATO
    @PostMapping("/inscricao/{inscricaoId}/aprovar")
    public ResponseEntity<?> aprovarCandidato(
            @AuthenticationPrincipal User user,
            @PathVariable Long inscricaoId
    ) {
        return atualizarStatusInscricao(user, inscricaoId, "APROVADO");
    }

    // 🔹 4. DECLINAR CANDIDATO
    @PostMapping("/inscricao/{inscricaoId}/declinar")
    public ResponseEntity<?> declinarCandidato(
            @AuthenticationPrincipal User user,
            @PathVariable Long inscricaoId
    ) {
        return atualizarStatusInscricao(user, inscricaoId, "REJEITADO");
    }

    // Função central para atualizar o status e verificar permissão
    private ResponseEntity<?> atualizarStatusInscricao(User user, Long inscricaoId, String novoStatus) {
        var usuarioOpt = usuarioRepo.findByEmail(user.getUsername());
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getEmpresa() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
        }
        Empresa empresaLogada = usuarioOpt.get().getEmpresa();

        Inscricao inscricao = inscricaoRepo.findById(inscricaoId)
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada."));

        // Garante que o Projeto e a Empresa do projeto estão carregados para a verificação
        if (inscricao.getProjeto() == null || inscricao.getProjeto().getEmpresa() == null ||
                !inscricao.getProjeto().getEmpresa().getId().equals(empresaLogada.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não é o proprietário deste projeto ou o projeto é inválido.");
        }

        // Atualiza o status
        inscricao.setStatus(novoStatus);
        inscricaoRepo.save(inscricao);

        return ResponseEntity.ok(novoStatus + " com sucesso.");
    }
}