package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.dto.LoginRequestDTO;
import FinTwin.EGR.demo.dto.LoginResponseDTO;
import FinTwin.EGR.demo.dto.RecuperarSenhaRequestDTO;
import FinTwin.EGR.demo.dto.RedefinirSenhaRequestDTO;
import FinTwin.EGR.demo.dto.UsuarioResponseDTO;
import FinTwin.EGR.demo.dto.Validacao2faRequestDTO;
import FinTwin.EGR.demo.exception.AutenticacaoException;
import FinTwin.EGR.demo.exception.RecursoNaoEncontradoException;
import FinTwin.EGR.demo.exception.RegraNegocioException;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.security.JwtService;
import FinTwin.EGR.demo.security.TotpService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TotpService totpService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       TotpService totpService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.totpService = totpService;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO autenticar(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new AutenticacaoException("Credenciais inválidas."));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new AutenticacaoException("Credenciais inválidas.");
        }

        if (Boolean.TRUE.equals(usuario.getSolicitouEsquecimento())) {
            throw new AutenticacaoException("Conta em processo de exclusão por direito ao esquecimento LGPD.");
        }

        if (Boolean.TRUE.equals(usuario.getDoisFatoresAtivo())) {
            return new LoginResponseDTO(true, UsuarioResponseDTO.fromEntity(usuario));
        }

        String token = jwtService.gerarToken(usuario.getEmail());
        return new LoginResponseDTO(token, UsuarioResponseDTO.fromEntity(usuario));
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO validar2fa(Validacao2faRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new AutenticacaoException("Credenciais inválidas."));

        if (Boolean.TRUE.equals(usuario.getSolicitouEsquecimento())
                || !Boolean.TRUE.equals(usuario.getDoisFatoresAtivo())
                || !totpService.validarCodigo(usuario.getTotpSecret(), dto.getCodigoTotp())) {
            throw new AutenticacaoException("Código 2FA inválido ou expirado.");
        }

        String token = jwtService.gerarToken(usuario.getEmail());
        return new LoginResponseDTO(token, UsuarioResponseDTO.fromEntity(usuario));
    }

    @Transactional
    public String solicitarRecuperacaoSenha(RecuperarSenhaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado para o email: " + dto.getEmail()));

        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacaoSenha(token);
        usuario.setExpiracaoTokenSenha(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        return token;
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacaoSenha(dto.getToken())
                .orElseThrow(() -> new RegraNegocioException("Token de recuperacao invalido ou inexistente."));

        if (usuario.getExpiracaoTokenSenha() == null || usuario.getExpiracaoTokenSenha().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Token de recuperacao expirado.");
        }

        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuario.setTokenRecuperacaoSenha(null);
        usuario.setExpiracaoTokenSenha(null);
        usuarioRepository.save(usuario);
    }
}
