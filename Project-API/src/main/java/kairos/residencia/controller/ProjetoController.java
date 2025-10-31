package kairos.residencia.controller;

import kairos.residencia.Dto.ProjetoDTO;
import kairos.residencia.response.InscricaoProjetoResponse;
import kairos.residencia.model.*;
import kairos.residencia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import kairos.residencia.response.ProjetoResponse;

import java.util.List;

@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoRepository projetoRepo;
    private final UsuarioRepository usuarioRepo;
    private final EmpresaRepository empresaRepo;
    private final AlunoRepository alunoRepo;
    private final InscricaoRepository inscricaoRepo;

    // 🔹 Listar todos (público) - APENAS ATIVOS com JOIN FETCH
    @GetMapping("/public")
    public ResponseEntity<List<ProjetoResponse>> listarPublico() {
        List<Projeto> projetosAtivos = projetoRepo.findAllActiveWithEmpresa();

        List<ProjetoResponse> projetos = projetosAtivos.stream()
                .map(p -> new ProjetoResponse(
                        p.getId(),
                        p.getNome(),
                        p.getDescricao(),
                        p.getDataCriacao(),
                        p.getEmpresa().getNome(),
                        p.isEncerrado(),
                        p.getTags(),
                        p.getRegime(),
                        p.getDataInicio(),
                        p.getDataFim(),
                        0L, // Contagem sempre zero para o público/aluno
                        0L  // Contagem sempre zero para o público/aluno
                ))
                .toList();
        return ResponseEntity.ok(projetos);
    }

    // 🔹 Listar projetos nos quais o aluno está inscrito
    @GetMapping("/inscricoes")
    public ResponseEntity<List<InscricaoProjetoResponse>> listarInscricoesAluno(@AuthenticationPrincipal User user) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Aluno aluno = alunoRepo.findByUsuario(usuario);
        if (aluno == null) {
            return ResponseEntity.status(403).body(List.of());
        }

        List<Inscricao> inscricoes = inscricaoRepo.findByAluno_Id(aluno.getId());

        List<InscricaoProjetoResponse> projetosInscritos = inscricoes.stream()
                .filter(inscricao -> inscricao.getProjeto() != null)
                .map(inscricao -> {
                    Projeto p = inscricao.getProjeto();

                    InscricaoProjetoResponse dto = new InscricaoProjetoResponse();

                    // Mapeia campos do Projeto
                    dto.setId(p.getId());
                    dto.setNome(p.getNome());
                    dto.setDescricao(p.getDescricao());
                    dto.setDataCriacao(p.getDataCriacao());
                    dto.setEmpresaNome(p.getEmpresa().getNome());
                    dto.setEncerrado(p.isEncerrado());
                    dto.setTags(p.getTags());
                    dto.setRegime(p.getRegime());
                    dto.setDataInicio(p.getDataInicio());
                    dto.setDataFim(p.getDataFim());

                    // ADICIONA O STATUS DA INSCRIÇÃO!
                    dto.setStatus(inscricao.getStatus());

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(projetosInscritos);
    }


    // 🔹 Criar projeto
    @PostMapping("/criar")
    public ResponseEntity<?> criarProjeto(
            @AuthenticationPrincipal User user,
            @RequestBody ProjetoDTO req
    ) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Empresa empresa = empresaRepo.findByUsuario(usuario);
        if (empresa == null) {
            return ResponseEntity.badRequest().body("Apenas empresas podem criar projetos");
        }
        Projeto novo = new Projeto();
        novo.setNome(req.getNome());
        novo.setDescricao(req.getDescricao());
        novo.setEmpresa(empresa);
        novo.setEncerrado(false);
        novo.setTags(req.getTags());
        novo.setRegime(req.getRegime());
        novo.setDataInicio(req.getDataInicio());
        novo.setDataFim(req.getDataFim());

        Projeto salvo = projetoRepo.save(novo);
        ProjetoResponse response = new ProjetoResponse(
                salvo.getId(),
                salvo.getNome(),
                salvo.getDescricao(),
                salvo.getDataCriacao(),
                salvo.getEmpresa().getNome(),
                salvo.isEncerrado(),
                salvo.getTags(),
                salvo.getRegime(),
                salvo.getDataInicio(),
                salvo.getDataFim(),
                0L, // Contagem zero
                0L  // Contagem zero
        );
        return ResponseEntity.ok(response);
    }

    // 🔹 Listar projetos da empresa logada
    @GetMapping("/meus")
    public ResponseEntity<?> listarMeusProjetos(@AuthenticationPrincipal User user) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Empresa empresa = empresaRepo.findByUsuario(usuario);
        if (empresa == null) {
            return ResponseEntity.status(403).body("Apenas empresas podem acessar seus projetos");
        }

        List<Projeto> projetos = projetoRepo.findByEmpresa(empresa);

        List<ProjetoResponse> meusProjetosDTO = projetos.stream()
                .map(p -> {
                    // Busca as contagens
                    Long totalCandidatos = inscricaoRepo.countByProjetoId(p.getId());
                    Long aprovados = inscricaoRepo.countAprovadosByProjetoId(p.getId());

                    // Mapeia para o DTO com os novos campos
                    return new ProjetoResponse(
                            p.getId(),
                            p.getNome(),
                            p.getDescricao(),
                            p.getDataCriacao(),
                            p.getEmpresa().getNome(),
                            p.isEncerrado(),
                            p.getTags(),
                            p.getRegime(),
                            p.getDataInicio(),
                            p.getDataFim(),
                            totalCandidatos,
                            aprovados
                    );
                })
                .toList();
        return ResponseEntity.ok(meusProjetosDTO);
    }


    // 🔹 Encerrar projeto
    @PostMapping("/{id}/encerrar")
    public ResponseEntity<String> encerrarProjeto(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Empresa empresaLogada = empresaRepo.findByUsuario(usuario);

        if (empresaLogada == null) {
            return ResponseEntity.status(403).body("Apenas empresas podem encerrar projetos");
        }

        Projeto projeto = projetoRepo.findByIdWithEmpresa(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        Long idEmpresaDoProjeto = projeto.getEmpresa() != null ? projeto.getEmpresa().getId() : null;

        if (idEmpresaDoProjeto == null || !idEmpresaDoProjeto.equals(empresaLogada.getId())) {
            return ResponseEntity.status(403).body("Você não tem permissão para encerrar este projeto");
        }

        projeto.setEncerrado(true);
        projetoRepo.save(projeto);

        return ResponseEntity.ok("Projeto encerrado com sucesso");
    }

    // 🔹 Inscrever-se no projeto (ENDPOINT DE INSCRIÇÃO)
    @PostMapping("/{id}/inscrever")
    public ResponseEntity<String> inscreverProjeto(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Aluno aluno = alunoRepo.findByUsuario(usuario);
        if (aluno == null) {
            return ResponseEntity.status(403).body("Apenas alunos podem se inscrever em projetos");
        }

        Projeto projeto = projetoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        if (projeto.isEncerrado()) {
            return ResponseEntity.badRequest().body("Este projeto está encerrado.");
        }

        if (inscricaoRepo.existsByProjeto_IdAndAluno_Id(projeto.getId(), aluno.getId())) {
            return ResponseEntity.badRequest().body("Você já está inscrito neste projeto.");
        }

        Inscricao novaInscricao = new Inscricao();
        novaInscricao.setAluno(aluno);
        novaInscricao.setProjeto(projeto);
        novaInscricao.setPapel("Participante");
        // Status inicial
        novaInscricao.setStatus("PENDENTE");

        inscricaoRepo.save(novaInscricao);

        return ResponseEntity.ok("Inscrição realizada com sucesso!");
    }

    // 🔹 Cancelar inscrição em projeto
    @DeleteMapping("/{id}/cancelar-inscricao")
    public ResponseEntity<String> cancelarInscricao(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long projetoId
    ) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Aluno aluno = alunoRepo.findByUsuario(usuario);
        if (aluno == null) {
            return ResponseEntity.status(403).body("Apenas alunos podem cancelar inscrições");
        }

        Inscricao inscricao = inscricaoRepo.findByProjeto_IdAndAluno_Id(projetoId, aluno.getId())
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada para este aluno e projeto"));

        // Verifica se o status permite cancelamento
        if (!"PENDENTE".equals(inscricao.getStatus())) {
            return ResponseEntity.badRequest().body("Não é possível cancelar uma inscrição que já foi " + inscricao.getStatus() + ".");
        }


        inscricaoRepo.delete(inscricao);

        return ResponseEntity.ok("Inscrição cancelada com sucesso.");
    }
}