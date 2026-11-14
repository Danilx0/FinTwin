package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.model.GemeoDigital;
import FinTwin.EGR.demo.model.PontoProjecao;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.GemeoDigitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class GemeoDigitalService {

    private final GemeoDigitalRepository gemeoDigitalRepository;

    
    public GemeoDigitalService(GemeoDigitalRepository gemeoDigitalRepository) {
        this.gemeoDigitalRepository = gemeoDigitalRepository;
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

        for (int ano = 1; ano <= horizonteAnos; ano++) {
            for (int mes = 1; mes <= 12; mes++) {
                patrimonioCorrente = patrimonioCorrente.multiply(BigDecimal.ONE.add(taxaMensal));
                patrimonioCorrente = patrimonioCorrente.add(aporteMensal);
                aportesAcumulados = aportesAcumulados.add(aporteMensal);
            }

            BigDecimal rendimentoAcumulado = patrimonioCorrente.subtract(patrimonioInicial).subtract(aportesAcumulados);

            PontoProjecao ponto = new PontoProjecao(
                    ano,
                    patrimonioCorrente.setScale(2, RoundingMode.HALF_UP),
                    rendimentoAcumulado.setScale(2, RoundingMode.HALF_UP),
                    aportesAcumulados.setScale(2, RoundingMode.HALF_UP)
            );

            gemeo.adicionarPontoProjecao(ponto);
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
}
