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

    // Dependências de Autenticação
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // Repositórios (injeções do @RequiredArgsConstructor)
    private final UsuarioRepository usuarioRepo;
    private final AlunoRepository alunoRepo;
    private final EmpresaRepository empresaRepo; // 👈 Adicionado
    private final InscricaoRepository inscricaoRepo; // 👈 Adicionado

    /**
     * Endpoint de Login (Corrigido)
     * Autentica o usuário e retorna o Token + PerfilDTO completo.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        // 1. Autentica o usuário
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String username = authentication.getName();

        // 2. Busca o objeto Usuario completo (COM PERFIL) do banco
        //    Usando o seu método otimizado com 'join fetch'
        Usuario usuario = usuarioRepo.findByEmailWithProfile(username) // ✅ MÉTODO CORRETO
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado após autenticação"));

        // 3. Gera o Token JWT
        String jwt = jwtUtil.generateToken(username, usuario.getRole());

        // 4. ✨ A CORREÇÃO ✨
        // Constroi o DTO de Perfil completo (com fotoUrl, etc.)
        PerfilDTO perfilCompleto = buildPerfilDTO(usuario);

        // 5. Retorna o novo objeto LoginResponse
        return ResponseEntity.ok(new LoginResponse(jwt, perfilCompleto));
    }

    /**
     * Endpoint de Registro
     * Cria um novo Usuário e um Aluno/Empresa associado.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {

        // Verifica se o email já existe
        if (usuarioRepo.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro: O email já está em uso!");
        }

        // Cria o novo Usuário
        Usuario usuario = new Usuario();
        usuario.setEmail(registerRequest.getEmail());
        usuario.setSenha(passwordEncoder.encode(registerRequest.getSenha()));

        // Define o Role (ex: "ROLE_ALUNO" ou "ROLE_EMPRESA")
        String role = "ROLE_" + registerRequest.getRole().toUpperCase();
        usuario.setRole(role);

        // Salva o usuário primeiro para ter um ID
        Usuario usuarioSalvo = usuarioRepo.save(usuario);

        // Cria a entidade específica (Aluno ou Empresa)
        try {
            if ("ROLE_ALUNO".equals(role)) {
                Aluno aluno = new Aluno();
                aluno.setUsuario(usuarioSalvo);
                aluno.setNome(registerRequest.getNome());
                aluno.setCurso(registerRequest.getCurso());
                aluno.setMatricula(registerRequest.getMatricula());
                alunoRepo.save(aluno);

                // Linka o aluno no usuário (JPA bidirecional)
                usuarioSalvo.setAluno(aluno);

            } else if ("ROLE_EMPRESA".equals(role)) {
                Empresa empresa = new Empresa();
                empresa.setUsuario(usuarioSalvo);
                empresa.setNome(registerRequest.getNome());
                empresa.setCnpj(registerRequest.getCnpj());
                empresaRepo.save(empresa);

                // Linka a empresa no usuário (JPA bidirecional)
                usuarioSalvo.setEmpresa(empresa);
            }

            // Atualiza o usuário com o link para aluno/empresa
            usuarioRepo.save(usuarioSalvo);

        } catch (Exception e) {
            // Em caso de erro (ex: matrícula duplicada), deleta o usuário criado
            usuarioRepo.delete(usuarioSalvo);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar perfil de aluno/empresa: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso!");
    }


    // -----------------------------------------------------
    // 🔹 MÉTODO COPIADO DO UsuarioController
    // -----------------------------------------------------
    // Este método é essencial para o endpoint de Login funcionar corretamente.

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