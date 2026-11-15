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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
                || !validarCodigoTotp(usuario.getTotpSecret(), dto.getCodigoTotp())) {
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

    private boolean validarCodigoTotp(String segredo, String codigo) {
        if (segredo == null || segredo.isBlank() || codigo == null || !codigo.matches("\\d{6}")) {
            return false;
        }

        try {
            byte[] chave = decodificarBase32(segredo);
            long intervaloAtual = System.currentTimeMillis() / 30_000L;
            for (long deslocamento = -1; deslocamento <= 1; deslocamento++) {
                byte[] contador = ByteBuffer.allocate(Long.BYTES).putLong(intervaloAtual + deslocamento).array();
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(new SecretKeySpec(chave, "HmacSHA1"));
                byte[] hash = mac.doFinal(contador);
                int indice = hash[hash.length - 1] & 0x0f;
                int valor = ((hash[indice] & 0x7f) << 24)
                        | ((hash[indice + 1] & 0xff) << 16)
                        | ((hash[indice + 2] & 0xff) << 8)
                        | (hash[indice + 3] & 0xff);
                String esperado = String.format(Locale.ROOT, "%06d", valor % 1_000_000);
                if (java.security.MessageDigest.isEqual(
                        esperado.getBytes(StandardCharsets.US_ASCII),
                        codigo.getBytes(StandardCharsets.US_ASCII))) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    private byte[] decodificarBase32(String segredo) {
        String alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String normalizado = segredo.replace("=", "").replace(" ", "").toUpperCase(Locale.ROOT);
        byte[] resultado = new byte[normalizado.length() * 5 / 8];
        int buffer = 0;
        int bitsDisponiveis = 0;
        int posicao = 0;

        for (char caractere : normalizado.toCharArray()) {
            int valor = alfabeto.indexOf(caractere);
            if (valor < 0) {
                throw new IllegalArgumentException("Segredo TOTP inválido.");
            }
            buffer = (buffer << 5) | valor;
            bitsDisponiveis += 5;
            if (bitsDisponiveis >= 8) {
                resultado[posicao++] = (byte) ((buffer >> (bitsDisponiveis - 8)) & 0xff);
                bitsDisponiveis -= 8;
            }
        }
        return Arrays.copyOf(resultado, posicao);
    }
}
