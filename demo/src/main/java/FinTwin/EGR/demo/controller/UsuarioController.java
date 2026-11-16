package FinTwin.EGR.demo.controller;

import FinTwin.EGR.demo.dto.CodigoTotpRequestDTO;
import FinTwin.EGR.demo.dto.Configuracao2faResponseDTO;
import FinTwin.EGR.demo.dto.PerfilAtualizacaoRequestDTO;
import FinTwin.EGR.demo.dto.UsuarioCadastroRequestDTO;
import FinTwin.EGR.demo.dto.UsuarioResponseDTO;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.TipoUsuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.service.FotoPerfilService;
import FinTwin.EGR.demo.service.PerfilUsuarioService;
import FinTwin.EGR.demo.service.SegurancaLgpdService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final SegurancaLgpdService segurancaLgpdService;
    private final PerfilUsuarioService perfilUsuarioService;
    private final FotoPerfilService fotoPerfilService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             SegurancaLgpdService segurancaLgpdService,
                             PerfilUsuarioService perfilUsuarioService,
                             FotoPerfilService fotoPerfilService,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.segurancaLgpdService = segurancaLgpdService;
        this.perfilUsuarioService = perfilUsuarioService;
        this.fotoPerfilService = fotoPerfilService;
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

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> consultarPerfil(@PathVariable Long id,
                                                              @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(id, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(perfilUsuarioService.buscar(id)));
    }

    /** UC05 - Gerenciar Perfil: nome, telefone e fuso horario. */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizarPerfil(@PathVariable Long id,
                                                              @Valid @RequestBody PerfilAtualizacaoRequestDTO dto,
                                                              @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(id, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(perfilUsuarioService.atualizarPerfil(id, dto)));
    }

    /** UC05 - envia ou troca a foto de perfil (JPG ou PNG, ate 5 MB). */
    @PostMapping(path = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioResponseDTO> enviarFoto(@PathVariable Long id,
                                                         @RequestParam("arquivo") MultipartFile arquivo,
                                                         @AuthenticationPrincipal Usuario usuarioAutenticado) throws IOException {
        if (!usuarioAutorizado(id, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(fotoPerfilService.salvar(id, arquivo.getBytes())));
    }

    @DeleteMapping("/{id}/foto")
    public ResponseEntity<UsuarioResponseDTO> removerFoto(@PathVariable Long id,
                                                          @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(id, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(fotoPerfilService.remover(id)));
    }

    /** A URL leva a versao (?v=), entao o navegador pode manter a imagem em cache sem risco de ficar desatualizada. */
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> consultarFoto(@PathVariable Long id,
                                                @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(id, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return fotoPerfilService.buscar(id)
                .map(foto -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(foto.getTipoConteudo()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePrivate().immutable())
                        .header("X-Content-Type-Options", "nosniff")
                        .body(foto.getConteudo()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** UC03 - passo 1: gera segredo TOTP e QR Code (2FA ainda inativo). */
    @PostMapping("/{id}/2fa/configurar")
    public ResponseEntity<Configuracao2faResponseDTO> configurar2fa(@PathVariable Long id,
                                                                    @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(id, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(perfilUsuarioService.iniciarConfiguracao2fa(id));
    }

    /** UC03 - passo 2: confirma o primeiro codigo do aplicativo autenticador e ativa o 2FA. */
    @PostMapping("/{id}/2fa/ativar")
    public ResponseEntity<UsuarioResponseDTO> ativar2fa(@PathVariable Long id,
                                                        @Valid @RequestBody CodigoTotpRequestDTO dto,
                                                        @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(id, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(perfilUsuarioService.ativar2fa(id, dto.getCodigoTotp())));
    }

    @PostMapping("/{id}/2fa/desativar")
    public ResponseEntity<UsuarioResponseDTO> desativar2fa(@PathVariable Long id,
                                                           @Valid @RequestBody CodigoTotpRequestDTO dto,
                                                           @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(id, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(perfilUsuarioService.desativar2fa(id, dto.getCodigoTotp())));
    }

    @PostMapping("/{id}/esquecimento")
    public ResponseEntity<?> solicitarEsquecimento(@PathVariable Long id,
                                                    @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(id, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        segurancaLgpdService.registrarPedidoEsquecimento(id);
        return ResponseEntity.ok(Map.of(
                "mensagem", "Solicitação de esquecimento registrada conforme LGPD.",
                "prazoMaximoHoras", 24
        ));
    }

    private boolean usuarioAutorizado(Long id, Usuario usuarioAutenticado) {
        return usuarioAutenticado != null && id.equals(usuarioAutenticado.getId());
    }
}
