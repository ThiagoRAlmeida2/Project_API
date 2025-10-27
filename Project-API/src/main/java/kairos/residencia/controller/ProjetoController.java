package kairos.residencia.controller;

import kairos.residencia.model.*;
import kairos.residencia.repository.*;
import kairos.residencia.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoRepository projetoRepo;

    // Listar todos projetos (público)
    @GetMapping("/public")
    public ResponseEntity<List<Projeto>> listarPublico() {
        return ResponseEntity.ok(projetoRepo.findAll());
    }

    // Criar projeto (público)
    @PostMapping("/public")
    public ResponseEntity<?> criarPublico(@RequestBody Projeto req) {
        Projeto salvo = projetoRepo.save(req);
        return ResponseEntity.ok(salvo);
    }

    // Encerrar projeto (público)
    @PostMapping("/public/{id}/encerrar")
    public ResponseEntity<?> encerrarPublico(@PathVariable Long id) {
        Projeto projeto = projetoRepo.findById(id).orElseThrow();
        projeto.setEncerrado(true); // precisa ter o campo 'encerrado' no Projeto
        projetoRepo.save(projeto);
        return ResponseEntity.ok("Projeto encerrado");
    }
}
