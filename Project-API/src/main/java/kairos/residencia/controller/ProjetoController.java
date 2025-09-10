package kairos.residencia.controller;

import kairos.residencia.model.*;
import kairos.residencia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
public class ProjetoController {
    private final ProjetoRepository projetoRepo;
    private final EmpresaRepository empresaRepo;
    private final UsuarioRepository usuarioRepo;
    private final AlunoRepository alunoRepo;
    private final InscricaoRepository inscricaoRepo;

    @PostMapping
    public ResponseEntity<?> criarProjeto(@AuthenticationPrincipal User user, @RequestBody Projeto req){
        var usuario = usuarioRepo.findByEmail(user.getUsername()).orElseThrow();
        var empresa = empresaRepo.findByUsuario_Id(usuario.getId()).orElseThrow(() -> new RuntimeException("Perfil empresa não encontrado"));
        req.setEmpresa(empresa);
        Projeto salvo = projetoRepo.save(req);
        return ResponseEntity.ok(salvo);
    }

    @GetMapping
    public ResponseEntity<?> listar(){
        return ResponseEntity.ok(projetoRepo.findAll());
    }

    @PostMapping("/{projetoId}/inscrever")
    public ResponseEntity<?> inscrever(@AuthenticationPrincipal User user, @PathVariable Long projetoId){
        var usuario = usuarioRepo.findByEmail(user.getUsername()).orElseThrow();
        var aluno = alunoRepo.findByUsuario_Id(usuario.getId()).orElseThrow(() -> new RuntimeException("Perfil aluno não encontrado"));
        if(inscricaoRepo.existsByProjeto_IdAndAluno_Id(projetoId, aluno.getId())) {
            return ResponseEntity.badRequest().body("Já inscrito");
        }
        var projeto = projetoRepo.findById(projetoId).orElseThrow();
        Inscricao i = new Inscricao();
        i.setAluno(aluno);
        i.setProjeto(projeto);
        i.setPapel("participante");
        inscricaoRepo.save(i);
        return ResponseEntity.ok("Inscrição realizada");
    }
}
