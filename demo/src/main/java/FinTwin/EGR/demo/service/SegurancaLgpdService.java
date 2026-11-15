package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SegurancaLgpdService {

    private final UsuarioRepository usuarioRepository;

    public SegurancaLgpdService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrarPedidoEsquecimento(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + usuarioId));

        usuario.solicitarDireitoAoEsquecimento();
        usuarioRepository.save(usuario);
    }

    
    @Transactional
    public int purgarDadosEsquecimentoLgpd() {
        LocalDateTime agora = LocalDateTime.now();
        List<Usuario> expirados = usuarioRepository.findBySolicitouEsquecimentoTrueAndDataLimiteExclusaoBefore(agora);

        for (Usuario u : expirados) {

            u.setNome("USUÁRIO_ANONIMIZADO");
            u.setEmail("anonimizado_" + u.getId() + "@fintwin.local");
            u.setSenha("");
            u.setFotoPerfilUrl(null);
            u.setTotpSecret(null);
            usuarioRepository.save(u);
        }

        return expirados.size();
    }
}
