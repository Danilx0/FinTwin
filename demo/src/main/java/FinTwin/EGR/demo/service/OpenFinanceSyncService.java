package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.model.ContaBancaria;
import FinTwin.EGR.demo.repository.ContaBancariaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OpenFinanceSyncService {

    private final ContaBancariaRepository contaBancariaRepository;
    
    
    public OpenFinanceSyncService(ContaBancariaRepository contaBancariaRepository) {
		this.contaBancariaRepository = contaBancariaRepository;
	}

	
    @Scheduled(fixedRate = 3600000) 
    @Transactional
    public void executarSincronizacaoPeriodica() {
        LocalDateTime agora = LocalDateTime.now();
        List<ContaBancaria> pendentes = contaBancariaRepository.findByDataProximaSincronizacaoBefore(agora);

        for (ContaBancaria conta : pendentes) {
            sincronizarConta(conta);
        }
    }

    @Transactional
    public void sincronizarConta(ContaBancaria conta) {

        conta.atualizarSincronizacaoOpenFinance();
        contaBancariaRepository.save(conta);
    }
}
