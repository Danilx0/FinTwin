package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.dto.Configuracao2faResponseDTO;
import FinTwin.EGR.demo.dto.PerfilAtualizacaoRequestDTO;
import FinTwin.EGR.demo.exception.RecursoNaoEncontradoException;
import FinTwin.EGR.demo.exception.RegraNegocioException;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.security.TotpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * UC05 - Gerenciar Perfil de Usuario e configuracao do UC03 - 2FA (TOTP).
 */
@Service
public class PerfilUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TotpService totpService;

    public PerfilUsuarioService(UsuarioRepository usuarioRepository, TotpService totpService) {
        this.usuarioRepository = usuarioRepository;
        this.totpService = totpService;
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
    }

    @Transactional
    public Usuario atualizarPerfil(Long usuarioId, PerfilAtualizacaoRequestDTO dto) {
        Usuario usuario = buscar(usuarioId);
        usuario.setNome(dto.getNome().trim());
        usuario.setTelefone(textoOuNulo(dto.getTelefone()));
        usuario.setFusoHorario(validarFusoHorario(dto.getFusoHorario()));
        return usuarioRepository.save(usuario);
    }

    /** Gera um novo segredo pendente; o 2FA so passa a valer depois de confirmado com um codigo. */
    @Transactional
    public Configuracao2faResponseDTO iniciarConfiguracao2fa(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        if (Boolean.TRUE.equals(usuario.getDoisFatoresAtivo())) {
            throw new RegraNegocioException("A autenticacao de dois fatores ja esta ativa.");
        }
        String segredo = totpService.gerarSegredo();
        usuario.setTotpSecret(segredo);
        usuarioRepository.save(usuario);

        String uri = totpService.gerarOtpauthUri(usuario.getEmail(), segredo);
        return new Configuracao2faResponseDTO(segredo, uri, totpService.gerarQrCodeDataUri(uri));
    }

    @Transactional
    public Usuario ativar2fa(Long usuarioId, String codigo) {
        Usuario usuario = buscar(usuarioId);
        if (Boolean.TRUE.equals(usuario.getDoisFatoresAtivo())) {
            throw new RegraNegocioException("A autenticacao de dois fatores ja esta ativa.");
        }
        if (usuario.getTotpSecret() == null) {
            throw new RegraNegocioException("Gere o QR Code antes de ativar o 2FA.");
        }
        if (!totpService.validarCodigo(usuario.getTotpSecret(), codigo)) {
            throw new RegraNegocioException("Codigo invalido. Confira o horario do celular e tente novamente.");
        }
        usuario.setDoisFatoresAtivo(true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario desativar2fa(Long usuarioId, String codigo) {
        Usuario usuario = buscar(usuarioId);
        if (!Boolean.TRUE.equals(usuario.getDoisFatoresAtivo())) {
            throw new RegraNegocioException("A autenticacao de dois fatores nao esta ativa.");
        }
        if (!totpService.validarCodigo(usuario.getTotpSecret(), codigo)) {
            throw new RegraNegocioException("Codigo invalido.");
        }
        usuario.setDoisFatoresAtivo(false);
        usuario.setTotpSecret(null);
        return usuarioRepository.save(usuario);
    }

    private String validarFusoHorario(String fusoHorario) {
        try {
            return ZoneId.of(fusoHorario.trim()).getId();
        } catch (DateTimeException e) {
            throw new RegraNegocioException("Fuso horario invalido: " + fusoHorario);
        }
    }

    private String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
