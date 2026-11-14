package FinTwin.EGR.demo.controller;

import FinTwin.EGR.demo.dto.UsuarioCadastroRequestDTO;
import FinTwin.EGR.demo.dto.UsuarioResponseDTO;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.service.SegurancaLgpdService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/acesso")
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

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody UsuarioCadastroRequestDTO dto) {
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

    @PostMapping("/validar-2fa/{id}")
    public ResponseEntity<?> validar2fa(@PathVariable Long id, @RequestParam String codigoTotp) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    boolean valido = segurancaLgpdService.validarCodigoTotp(u, codigoTotp);
                    return ResponseEntity.ok(Map.of("valido", valido));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/lgpd/solicitar-esquecimento/{id}")
    public ResponseEntity<?> solicitarEsquecimento(@PathVariable Long id) {
        segurancaLgpdService.registrarPedidoEsquecimento(id);
        return ResponseEntity.ok(Map.of(
                "mensagem", "Solicitação de esquecimento registrada conforme LGPD.",
                "prazoMaximoHoras", 24
        ));
    }

    @GetMapping("/perfil/{id}")
    public ResponseEntity<?> consultarPerfil(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(u -> ResponseEntity.ok(UsuarioResponseDTO.fromEntity(u)))
                .orElse(ResponseEntity.notFound().build());
    }
}
