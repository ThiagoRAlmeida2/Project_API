package kairos.residencia.controller;

import kairos.residencia.Dto.ProjetoDTO;
import kairos.residencia.model.*;
import kairos.residencia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoRepository projetoRepo;
    private final UsuarioRepository usuarioRepo;
    private final EmpresaRepository empresaRepo;
    private final AlunoRepository alunoRepo; // 🚩 NOVO REPO INJETADO
    private final InscricaoRepository inscricaoRepo; // 🚩 NOVO REPO INJETADO


    public record ProjetoResponse(Long id, String nome, String descricao, LocalDateTime dataCriacao, String empresaNome, boolean encerrado) {}

    // 🔹 Listar todos (público) - APENAS ATIVOS com JOIN FETCH
    @GetMapping("/public")
    public ResponseEntity<List<ProjetoResponse>> listarPublico() {
        // Usa o método que carrega a Empresa e filtra por ativos
        List<Projeto> projetosAtivos = projetoRepo.findAllActiveWithEmpresa();

        List<ProjetoResponse> projetos = projetosAtivos.stream()
                .map(p -> new ProjetoResponse(
                        p.getId(),
                        p.getNome(),
                        p.getDescricao(),
                        p.getDataCriacao(),
                        p.getEmpresa().getNome(),
                        p.isEncerrado()
                ))
                .toList();

        return ResponseEntity.ok(projetos);
    }

    // 🔹 Listar projetos nos quais o aluno está inscrito (NOVO ENDPOINT)
    @GetMapping("/inscricoes")
    public ResponseEntity<?> listarInscricoesAluno(@AuthenticationPrincipal User user) {
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Aluno aluno = alunoRepo.findByUsuario(usuario);
        if (aluno == null) {
            return ResponseEntity.status(403).body("Apenas alunos podem acessar suas inscrições");
        }

        List<Inscricao> inscricoes = inscricaoRepo.findByAluno_Id(aluno.getId());

        // Mapeia as inscrições para a lista de ProjetosResponse
        List<ProjetoResponse> projetosInscritos = inscricoes.stream()
                .map(inscricao -> {
                    Projeto p = inscricao.getProjeto();
                    return new ProjetoResponse(
                            p.getId(),
                            p.getNome(),
                            p.getDescricao(),
                            p.getDataCriacao(),
                            p.getEmpresa().getNome(),
                            p.isEncerrado()
                    );
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
        // ... (código existente)
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

        Projeto salvo = projetoRepo.save(novo);

        ProjetoResponse response = new ProjetoResponse(
                salvo.getId(),
                salvo.getNome(),
                salvo.getDescricao(),
                salvo.getDataCriacao(),
                salvo.getEmpresa().getNome(),
                salvo.isEncerrado()
        );

        return ResponseEntity.ok(response);
    }

    // 🔹 Listar projetos da empresa logada
    @GetMapping("/meus")
    public ResponseEntity<?> listarMeusProjetos(@AuthenticationPrincipal User user) {
        // ... (código existente)
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Empresa empresa = empresaRepo.findByUsuario(usuario);
        if (empresa == null) {
            return ResponseEntity.status(403).body("Apenas empresas podem acessar seus projetos");
        }

        List<Projeto> projetos = projetoRepo.findByEmpresa(empresa);

        List<ProjetoResponse> meusProjetosDTO = projetos.stream()
                .map(p -> new ProjetoResponse(
                        p.getId(),
                        p.getNome(),
                        p.getDescricao(),
                        p.getDataCriacao(),
                        p.getEmpresa().getNome(),
                        p.isEncerrado()
                ))
                .toList();

        return ResponseEntity.ok(meusProjetosDTO);
    }


    // 🔹 Encerrar projeto
    @PostMapping("/{id}/encerrar")
    public ResponseEntity<?> encerrarProjeto(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        // ... (código existente)
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
    public ResponseEntity<?> inscreverProjeto(
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

        inscricaoRepo.save(novaInscricao);

        return ResponseEntity.ok("Inscrição realizada com sucesso!");
    }
    // 🔹 Cancelar inscrição em projeto (apenas aluno autenticado) 🚩 NOVO ENDPOINT
    @DeleteMapping("/{id}/cancelar-inscricao")
    public ResponseEntity<?> cancelarInscricao(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long projetoId
    ) {
        // 1. Encontrar o usuário e aluno logado
        Usuario usuario = usuarioRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Aluno aluno = alunoRepo.findByUsuario(usuario);
        if (aluno == null) {
            return ResponseEntity.status(403).body("Apenas alunos podem cancelar inscrições");
        }

        // 2. Encontrar a inscrição específica
        Inscricao inscricao = inscricaoRepo.findByProjeto_IdAndAluno_Id(projetoId, aluno.getId())
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada para este aluno e projeto"));

        // 3. Deletar a inscrição
        inscricaoRepo.delete(inscricao);

        return ResponseEntity.ok("Inscrição cancelada com sucesso.");
    }
}