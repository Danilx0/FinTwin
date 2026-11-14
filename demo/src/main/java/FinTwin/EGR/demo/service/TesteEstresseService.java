package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.model.CenarioEstresse;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.CenarioEstresseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TesteEstresseService {

    private final CenarioEstresseRepository cenarioEstresseRepository;

    public TesteEstresseService(CenarioEstresseRepository cenarioEstresseRepository) {
        this.cenarioEstresseRepository = cenarioEstresseRepository;
    }

    @Transactional
    public CenarioEstresse simularCriseRenda(Usuario usuario, String titulo, BigDecimal rendaMensalAtual,
                                             BigDecimal despesaMensalAtual, BigDecimal reservaEmergenciaAtual,
                                             BigDecimal percentualCorteRenda, int duracaoMeses) {

        BigDecimal fatorCorte = percentualCorteRenda.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal novaRenda = rendaMensalAtual.multiply(BigDecimal.ONE.subtract(fatorCorte));
        BigDecimal deficitMensal = despesaMensalAtual.subtract(novaRenda);

        BigDecimal impactoPatrimonial;
        int mesesSobrevivencia = 0;

        if (deficitMensal.compareTo(BigDecimal.ZERO) > 0) {
            impactoPatrimonial = deficitMensal.multiply(BigDecimal.valueOf(duracaoMeses));
            if (despesaMensalAtual.compareTo(BigDecimal.ZERO) > 0) {
                mesesSobrevivencia = reservaEmergenciaAtual.divide(deficitMensal, 0, RoundingMode.FLOOR).intValue();
            }
        } else {
            impactoPatrimonial = BigDecimal.ZERO;
            mesesSobrevivencia = 999;
        }

        CenarioEstresse cenario = new CenarioEstresse(usuario, titulo, percentualCorteRenda, duracaoMeses);
        cenario.setImpactoPatrimonialEstimado(impactoPatrimonial);
        cenario.setMesesSobrevivenciaReserva(mesesSobrevivencia);

        return cenarioEstresseRepository.save(cenario);
    }
}
