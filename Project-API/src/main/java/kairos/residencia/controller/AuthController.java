package kairos.residencia.controller;

import kairos.residencia.Dto.ResetSenhaRequest;
import kairos.residencia.model.Aluno;
import kairos.residencia.model.Empresa;
import kairos.residencia.model.Usuario;
import kairos.residencia.repository.UsuarioRepository;
import kairos.residencia.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req){

        // verifica se já existe
        if(usuarioRepo.findByEmail(req.getEmail()).isPresent()){
            return ResponseEntity.badRequest().body("Email já cadastrado");
        }

        // cria usuário
        Usuario u = new Usuario();
        u.setEmail(req.getEmail());
        u.setSenha(passwordEncoder.encode(req.getSenha()));
        u.setRole(req.getRole()); // role string já deve ser "ROLE_ALUNO" ou "ROLE_EMPRESA"

        // cria perfil de aluno ou empresa
        if("ROLE_ALUNO".equals(req.getRole())){
            Aluno a = new Aluno();
            a.setNome(req.getNome());
            a.setCurso(req.getCurso());
            a.setMatricula(req.getMatricula());
            a.setUsuario(u);
            u.setAluno(a);
        } else if("ROLE_EMPRESA".equals(req.getRole())){
            Empresa e = new Empresa();
            e.setNome(req.getNome());
            e.setCnpj(req.getCnpj());
            e.setUsuario(u);
            u.setEmpresa(e);
        }

        usuarioRepo.save(u);
        return ResponseEntity.ok("Registrado com sucesso");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req){
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getSenha())
            );

            Usuario u = usuarioRepo.findByEmail(req.getEmail()).get();
            String token = jwtUtil.generateToken(u.getEmail(), u.getRole());

            return ResponseEntity.ok(new kairos.residencia.controller.AuthResponse(token, u.getEmail(), u.getRole()));

        } catch (BadCredentialsException ex){
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
    }

    @PostMapping("/resetar-senha")
    public ResponseEntity<?> resetarSenha(@RequestBody ResetSenhaRequest req) {
        var usuario = usuarioRepo.findByEmail(req.getEmail()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuário não encontrado"));
        }

        usuario.setSenha(passwordEncoder.encode(req.getNovaSenha()));
        usuarioRepo.save(usuario);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso!"));
    }

}
