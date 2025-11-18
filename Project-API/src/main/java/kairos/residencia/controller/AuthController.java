package kairos.residencia.controller;

import kairos.residencia.Dto.PerfilDTO;
import kairos.residencia.response.LoginResponse;
import kairos.residencia.model.*;
import kairos.residencia.repository.AlunoRepository;
import kairos.residencia.repository.EmpresaRepository;
import kairos.residencia.repository.InscricaoRepository;
import kairos.residencia.repository.UsuarioRepository;
import kairos.residencia.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private final UsuarioRepository usuarioRepo;
    private final AlunoRepository alunoRepo;
    private final EmpresaRepository empresaRepo;
    private final InscricaoRepository inscricaoRepo;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String username = authentication.getName();

        Usuario usuario = usuarioRepo.findByEmailWithProfile(username) // ✅ MÉTODO CORRETO
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado após autenticação"));

        String jwt = jwtUtil.generateToken(username, usuario.getRole());

        PerfilDTO perfilCompleto = buildPerfilDTO(usuario);

        return ResponseEntity.ok(new LoginResponse(jwt, perfilCompleto));
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req){

        if(usuarioRepo.findByEmail(req.getEmail()).isPresent()){
            return ResponseEntity.badRequest().body("Email já cadastrado");
        }

        Usuario u = new Usuario();
        u.setEmail(req.getEmail());
        u.setSenha(passwordEncoder.encode(req.getSenha()));
        u.setRole(req.getRole());

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
            a.setFotoUrl(u.getAluno().getFotoUrl()); // 👈 A FOTO VEM AQUI

            // Mapeia Projetos Participados
            List<Inscricao> inscricoes = inscricaoRepo.findByAluno_Id(u.getAluno().getId());
            List<PerfilDTO.ProjetoParticipadoDTO> projetos = inscricoes.stream()
                    .filter(i -> i.getProjeto() != null)
                    .map(i -> {
                        Projeto p = i.getProjeto();
                        PerfilDTO.ProjetoParticipadoDTO pDto = new PerfilDTO.ProjetoParticipadoDTO();
                        pDto.setId(p.getId());
                        pDto.setNome(p.getNome());
                        // Verificação de segurança para evitar NullPointerException
                        if (p.getEmpresa() != null) {
                            pDto.setEmpresaNome(p.getEmpresa().getNome());
                        }
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
            e.setFotoUrl(u.getEmpresa().getFotoUrl()); // 👈 A FOTO VEM AQUI
            dto.setEmpresa(e);
        }
        return dto;
    }
}