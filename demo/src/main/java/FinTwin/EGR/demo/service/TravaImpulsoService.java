package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.exception.RecursoNaoEncontradoException;
import FinTwin.EGR.demo.model.Transacao;
import FinTwin.EGR.demo.model.TravaImpulso;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import FinTwin.EGR.demo.model.enums.StatusTravaImpulso;
import FinTwin.EGR.demo.model.enums.TipoTransacao;
import FinTwin.EGR.demo.repository.TransacaoRepository;
import FinTwin.EGR.demo.repository.TravaImpulsoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UC10 - Trava de Impulso: o gasto manual so e efetivado (vira transacao) depois das 24 horas de quarentena.
 */
@Service
public class TravaImpulsoService {

    private final TravaImpulsoRepository travaImpulsoRepository;
    private final TransacaoRepository transacaoRepository;
    private final CategorizacaoIaService categorizacaoIaService;

    public TravaImpulsoService(TravaImpulsoRepository travaImpulsoRepository,
                               TransacaoRepository transacaoRepository,
                               CategorizacaoIaService categorizacaoIaService) {
        this.travaImpulsoRepository = travaImpulsoRepository;
        this.transacaoRepository = transacaoRepository;
        this.categorizacaoIaService = categorizacaoIaService;
    }

    @Transactional
    public TravaImpulso submeterAoModoQuarentena(Usuario usuario, String itemDesejado, BigDecimal valor, String justificativa) {
        TravaImpulso trava = new TravaImpulso(usuario, itemDesejado, valor, justificativa);
        return travaImpulsoRepository.save(trava);
    }

    @Transactional(readOnly = true)
    public TravaImpulso buscar(Long travaId) {
        return travaImpulsoRepository.findById(travaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trava de impulso não encontrada: " + travaId));
    }

    /** Confirma a compra apos a quarentena e registra a despesa correspondente. */
    @Transactional
    public TravaImpulso liberarGasto(Long travaId) {
        TravaImpulso trava = buscar(travaId);
        exigirEmQuarentena(trava);

        if (!trava.podeSerLiberada()) {
            throw new IllegalStateException("Trava de Impulso ativa: ainda não se passaram as 24 horas obrigatórias de quarentena.");
        }

        trava.setStatus(StatusTravaImpulso.LIBERADA);
        trava.setDataDecisaoFinal(LocalDateTime.now());

        CategoriaTransacao categoria = categorizacaoIaService.sugerirCategoria(trava.getItemDesejado());
        Transacao despesa = new Transacao(trava.getUsuario(), trava.getItemDesejado(), trava.getValor(),
                TipoTransacao.DESPESA, categoria);
        despesa.setCategoriaSugeridaIa(categoria);
        transacaoRepository.save(despesa);

        return travaImpulsoRepository.save(trava);
    }

    /** Desistencia: pode acontecer a qualquer momento durante a quarentena. */
    @Transactional
    public TravaImpulso cancelarGasto(Long travaId) {
        TravaImpulso trava = buscar(travaId);
        exigirEmQuarentena(trava);

        trava.setStatus(StatusTravaImpulso.CANCELADA);
        trava.setDataDecisaoFinal(LocalDateTime.now());
        return travaImpulsoRepository.save(trava);
    }

    @Transactional(readOnly = true)
    public List<TravaImpulso> listarPorUsuario(Long usuarioId) {
        return travaImpulsoRepository.findByUsuarioIdOrderByDataEntradaQuarentenaDesc(usuarioId);
    }

    private void exigirEmQuarentena(TravaImpulso trava) {
        if (trava.getStatus() != StatusTravaImpulso.QUARENTENA) {
            throw new IllegalStateException("Esta trava já foi decidida (" + trava.getStatus().name().toLowerCase() + ").");
        }
    }
}
