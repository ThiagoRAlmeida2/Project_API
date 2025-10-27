package kairos.residencia.controller;

import kairos.residencia.Dto.PerfilDTO;
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

        // Criar DTO
        PerfilDTO dto = new PerfilDTO();
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());

        if(u.getAluno() != null) {
            PerfilDTO.AlunoDTO a = new PerfilDTO.AlunoDTO();
            a.setNome(u.getAluno().getNome());
            a.setCurso(u.getAluno().getCurso());
            a.setMatricula(u.getAluno().getMatricula());
            dto.setAluno(a);
        }

        if(u.getEmpresa() != null) {
            PerfilDTO.EmpresaDTO e = new PerfilDTO.EmpresaDTO();
            e.setNome(u.getEmpresa().getNome());
            e.setCnpj(u.getEmpresa().getCnpj());
            dto.setEmpresa(e);
        }

        return ResponseEntity.ok(dto);
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
        } else if ("ROLE_EMPRESA".equals(u.getRole()) && u.getEmpresa() != null) {
            u.getEmpresa().setNome(req.getNome());
            u.getEmpresa().setCnpj(req.getCnpj());
        }

        usuarioRepo.save(u);

        // Retornar DTO atualizado
        PerfilDTO dto = new PerfilDTO();
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        if(u.getAluno() != null){
            PerfilDTO.AlunoDTO a = new PerfilDTO.AlunoDTO();
            a.setNome(u.getAluno().getNome());
            a.setCurso(u.getAluno().getCurso());
            a.setMatricula(u.getAluno().getMatricula());
            dto.setAluno(a);
        }
        if(u.getEmpresa() != null){
            PerfilDTO.EmpresaDTO e = new PerfilDTO.EmpresaDTO();
            e.setNome(u.getEmpresa().getNome());
            e.setCnpj(u.getEmpresa().getCnpj());
            dto.setEmpresa(e);
        }

        return ResponseEntity.ok(dto);
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMe(@AuthenticationPrincipal User user){
        var opt = usuarioRepo.findByEmail(user.getUsername());
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        usuarioRepo.delete(opt.get()); // cascata vai apagar perfil e entidades vinculadas
        return ResponseEntity.ok("Perfil e dados associados deletados");
    }
}