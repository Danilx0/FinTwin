package FinTwin.EGR.demo.controller;

import FinTwin.EGR.demo.dto.UsuarioCadastroRequestDTO;
import FinTwin.EGR.demo.dto.UsuarioResponseDTO;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.TipoUsuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.service.SegurancaLgpdService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final SegurancaLgpdService segurancaLgpdService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             SegurancaLgpdService segurancaLgpdService,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.segurancaLgpdService = segurancaLgpdService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody UsuarioCadastroRequestDTO dto) {
        if (dto.getTipoUsuario() == TipoUsuario.CONSULTOR || dto.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "O tipo de usuário solicitado não pode ser criado por cadastro público."));
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "E-mail já cadastrado."));
        }

        Usuario usuario = new Usuario(
                dto.getNome(),
                dto.getEmail(),
                passwordEncoder.encode(dto.getSenha()),
                dto.getTipoUsuario()
        );

        Usuario salvo = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponseDTO.fromEntity(salvo));
    }

    @PostMapping("/{id}/esquecimento")
    public ResponseEntity<?> solicitarEsquecimento(@PathVariable Long id,
                                                    @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null || !id.equals(usuarioAutenticado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        segurancaLgpdService.registrarPedidoEsquecimento(id);
        return ResponseEntity.ok(Map.of(
                "mensagem", "Solicitação de esquecimento registrada conforme LGPD.",
                "prazoMaximoHoras", 24
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> consultarPerfil(@PathVariable Long id,
                                             @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null || !id.equals(usuarioAutenticado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return usuarioRepository.findById(id)
                .map(u -> ResponseEntity.ok(UsuarioResponseDTO.fromEntity(u)))
                .orElse(ResponseEntity.notFound().build());
    }
}
