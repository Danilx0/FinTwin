package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.model.TravaImpulso;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.StatusTravaImpulso;
import FinTwin.EGR.demo.repository.TravaImpulsoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TravaImpulsoService {

    private final TravaImpulsoRepository travaImpulsoRepository;

    public TravaImpulsoService(TravaImpulsoRepository travaImpulsoRepository) {
        this.travaImpulsoRepository = travaImpulsoRepository;
    }

    @Transactional
    public TravaImpulso submeterAoModoQuarentena(Usuario usuario, String itemDesejado, BigDecimal valor, String justificativa) {
        TravaImpulso trava = new TravaImpulso(usuario, itemDesejado, valor, justificativa);
        return travaImpulsoRepository.save(trava);
    }

    @Transactional
    public TravaImpulso liberarGasto(Long travaId) {
        TravaImpulso trava = travaImpulsoRepository.findById(travaId)
                .orElseThrow(() -> new IllegalArgumentException("Trava de impulso não encontrada: " + travaId));

        if (!trava.podeSerLiberada()) {
            throw new IllegalStateException("Trava de Impulso ativa: ainda não se passaram as 24 horas obrigatórias de quarentena.");
        }

        trava.setStatus(StatusTravaImpulso.LIBERADA);
        trava.setDataDecisaoFinal(LocalDateTime.now());
        return travaImpulsoRepository.save(trava);
    }

    @Transactional
    public TravaImpulso cancelarGasto(Long travaId) {
        TravaImpulso trava = travaImpulsoRepository.findById(travaId)
                .orElseThrow(() -> new IllegalArgumentException("Trava de impulso não encontrada: " + travaId));

        trava.setStatus(StatusTravaImpulso.CANCELADA);
        trava.setDataDecisaoFinal(LocalDateTime.now());
        return travaImpulsoRepository.save(trava);
    }

    public List<TravaImpulso> listarQuarentenasAtivas(Long usuarioId) {
        return travaImpulsoRepository.findByUsuarioIdAndStatus(usuarioId, StatusTravaImpulso.QUARENTENA);
    }
}
