package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.model.GemeoDigital;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.dto.PontoProjecaoDTO;
import FinTwin.EGR.demo.exception.RecursoNaoEncontradoException;
import FinTwin.EGR.demo.exception.RegraNegocioException;
import FinTwin.EGR.demo.repository.GemeoDigitalRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class GemeoDigitalService {

    private final GemeoDigitalRepository gemeoDigitalRepository;
    private final JsonMapper jsonMapper;

    public GemeoDigitalService(GemeoDigitalRepository gemeoDigitalRepository, JsonMapper jsonMapper) {
        this.gemeoDigitalRepository = gemeoDigitalRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public GemeoDigital projetarPatrimonio(Usuario usuario, String nomeCenario, BigDecimal patrimonioInicial, 
                                           BigDecimal aporteMensal, BigDecimal taxaRendimentoAnual) {
        return projetarPatrimonio(usuario, nomeCenario, 35, patrimonioInicial, aporteMensal, taxaRendimentoAnual, BigDecimal.valueOf(4.0));
    }

    @Transactional
    public GemeoDigital projetarPatrimonio(Usuario usuario, String nomeCenario, int horizonteAnos,
                                           BigDecimal patrimonioInicial, BigDecimal aporteMensal,
                                           BigDecimal taxaRendimentoAnual, BigDecimal inflacaoAnual) {
        long inicioTempo = System.currentTimeMillis();

        if (horizonteAnos > 35) {
            throw new IllegalArgumentException("O horizonte máximo do Gêmeo Digital é de 35 anos.");
        }
        if (horizonteAnos < 1) {
            throw new IllegalArgumentException("O horizonte mínimo do Gêmeo Digital é de 1 ano.");
        }

        GemeoDigital gemeo = new GemeoDigital(
                usuario, 
                nomeCenario, 
                horizonteAnos, 
                patrimonioInicial, 
                aporteMensal, 
                taxaRendimentoAnual, 
                inflacaoAnual
        );

        BigDecimal taxaMensal = taxaRendimentoAnual.divide(BigDecimal.valueOf(1200), 8, RoundingMode.HALF_UP);
        BigDecimal patrimonioCorrente = patrimonioInicial;
        BigDecimal aportesAcumulados = BigDecimal.ZERO;
        List<PontoProjecaoDTO> pontosProjecao = new ArrayList<>(horizonteAnos);

        for (int ano = 1; ano <= horizonteAnos; ano++) {
            for (int mes = 1; mes <= 12; mes++) {
                patrimonioCorrente = patrimonioCorrente.multiply(BigDecimal.ONE.add(taxaMensal));
                patrimonioCorrente = patrimonioCorrente.add(aporteMensal);
                aportesAcumulados = aportesAcumulados.add(aporteMensal);
            }

            BigDecimal rendimentoAcumulado = patrimonioCorrente.subtract(patrimonioInicial).subtract(aportesAcumulados);

            PontoProjecaoDTO ponto = new PontoProjecaoDTO(
                    ano,
                    patrimonioCorrente.setScale(2, RoundingMode.HALF_UP),
                    rendimentoAcumulado.setScale(2, RoundingMode.HALF_UP),
                    aportesAcumulados.setScale(2, RoundingMode.HALF_UP)
            );

            pontosProjecao.add(ponto);
        }

        try {
            gemeo.setResultadoProjecaoJson(jsonMapper.writeValueAsString(pontosProjecao));
        } catch (JacksonException e) {
            throw new RegraNegocioException("Não foi possível serializar o resultado da projeção.");
        }

        long duracaoMs = System.currentTimeMillis() - inicioTempo;
        gemeo.setTempoProcessamentoMs(duracaoMs);

        if (duracaoMs > 4000) {
            System.err.println("ALERTA: Processamento do Gêmeo Digital excedeu o limite de 4 segundos: " + duracaoMs + " ms");
        }

        return gemeoDigitalRepository.save(gemeo);
    }
    public List<GemeoDigital> listarPorUsuario(Long usuarioId) {
        return gemeoDigitalRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public GemeoDigital buscar(Long gemeoId) {
        return gemeoDigitalRepository.findById(gemeoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeção não encontrada."));
    }

    @Transactional
    public void excluir(Long gemeoId) {
        gemeoDigitalRepository.delete(buscar(gemeoId));
    }
}
