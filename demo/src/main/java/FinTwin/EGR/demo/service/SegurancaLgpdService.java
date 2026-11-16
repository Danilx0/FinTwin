package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.exception.RecursoNaoEncontradoException;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Direito ao esquecimento (LGPD): o pedido bloqueia o acesso na hora e os dados
 * sao apagados em ate 24 horas pelo job agendado.
 */
@Service
public class SegurancaLgpdService {

    private static final Logger log = LoggerFactory.getLogger(SegurancaLgpdService.class);

    private final UsuarioRepository usuarioRepository;

    public SegurancaLgpdService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrarPedidoEsquecimento(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + usuarioId));

        if (Boolean.TRUE.equals(usuario.getSolicitouEsquecimento())) {
            return;
        }
        usuario.solicitarDireitoAoEsquecimento();
        usuarioRepository.save(usuario);
    }

    /**
     * Executa a cada 15 minutos. Apagar o usuario remove em cascata (ON DELETE CASCADE na V1)
     * contas, transacoes, simulacoes, travas, relatorios e historico de IA.
     */
    @Scheduled(fixedDelayString = "${fintwin.lgpd.intervalo-ms:900000}", initialDelayString = "${fintwin.lgpd.atraso-inicial-ms:60000}")
    @Transactional
    public int purgarDadosEsquecimentoLgpd() {
        List<Usuario> expirados = usuarioRepository
                .findBySolicitouEsquecimentoTrueAndDataLimiteExclusaoBefore(LocalDateTime.now());

        if (!expirados.isEmpty()) {
            usuarioRepository.deleteAll(expirados);
            usuarioRepository.flush();
            log.info("LGPD: {} conta(s) excluida(s) por direito ao esquecimento.", expirados.size());
        }
        return expirados.size();
    }
}
