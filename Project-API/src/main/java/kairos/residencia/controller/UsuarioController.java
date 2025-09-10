package kairos.residencia.controller;

import kairos.residencia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioRepository usuarioRepo;

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user){
        var u = usuarioRepo.findByEmail(user.getUsername()).orElse(null);
        if(u==null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMe(@AuthenticationPrincipal User user){
        var opt = usuarioRepo.findByEmail(user.getUsername());
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        usuarioRepo.delete(opt.get()); // cascata vai apagar perfil e entidades vinculadas
        return ResponseEntity.ok("Perfil e dados associados deletados");
    }
}