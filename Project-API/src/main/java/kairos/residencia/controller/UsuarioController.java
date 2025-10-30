package kairos.residencia.controller;

import kairos.residencia.Dto.PerfilDTO;
import kairos.residencia.model.Inscricao;
import kairos.residencia.model.Projeto;
import kairos.residencia.repository.InscricaoRepository;
import kairos.residencia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
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

    // 🔹 Endpoint GET /me: Carrega o perfil completo
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user){
        var u = usuarioRepo.findByEmail(user.getUsername()).orElse(null);
        if(u==null) return ResponseEntity.notFound().build();

        PerfilDTO dto = new PerfilDTO();
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());

        if(u.getAluno() != null) {
            PerfilDTO.AlunoDTO a = new PerfilDTO.AlunoDTO();
            a.setNome(u.getAluno().getNome());
            a.setCurso(u.getAluno().getCurso());
            a.setMatricula(u.getAluno().getMatricula());

            // Mapeia Descrição e Tags
            a.setDescricao(u.getAluno().getDescricao());
            a.setTags(u.getAluno().getTags());

            // Mapeia Projetos Participados
            List<Inscricao> inscricoes = inscricaoRepo.findByAluno_Id(u.getAluno().getId());
            List<PerfilDTO.ProjetoParticipadoDTO> projetos = inscricoes.stream()
                    // Garante que o Projeto existe antes de mapear
                    .filter(i -> i.getProjeto() != null)
                    .map(i -> {
                        Projeto p = i.getProjeto();

                        PerfilDTO.ProjetoParticipadoDTO pDto = new PerfilDTO.ProjetoParticipadoDTO();
                        pDto.setId(p.getId());
                        pDto.setNome(p.getNome());

                        // Oculto no frontend, mas necessário para a lógica do backend
                        pDto.setEmpresaNome(p.getEmpresa().getNome());

                        pDto.setDataInicio(p.getDataInicio());

                        // NOVOS CAMPOS DO PROJETO PARA EXIBIÇÃO NO PERFIL
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

        if(u.getEmpresa() != null) {
            PerfilDTO.EmpresaDTO e = new PerfilDTO.EmpresaDTO();
            e.setNome(u.getEmpresa().getNome());
            e.setCnpj(u.getEmpresa().getCnpj());
            dto.setEmpresa(e);
        }

        return ResponseEntity.ok(dto);
    }

    // 🔹 Endpoint PUT /me: Atualiza o perfil
    @PutMapping("/me")
    public ResponseEntity<?> atualizarPerfil(
            @AuthenticationPrincipal User user,
            @RequestBody UpdatePerfilRequest req
    ) {
        var u = usuarioRepo.findByEmail(user.getUsername()).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();

        if ("ROLE_ALUNO".equals(u.getRole()) && u.getAluno() != null) {
            // Salva os campos existentes e novos
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

        // Retorna o DTO atualizado
        return me(user);
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMe(@AuthenticationPrincipal User user){
        var opt = usuarioRepo.findByEmail(user.getUsername());
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        usuarioRepo.delete(opt.get());
        return ResponseEntity.ok("Perfil e dados associados deletados");
    }
}