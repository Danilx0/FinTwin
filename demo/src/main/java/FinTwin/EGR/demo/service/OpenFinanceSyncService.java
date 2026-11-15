package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.dto.ContaBancariaDTO;
import FinTwin.EGR.demo.model.ContaBancaria;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.repository.ContaBancariaRepository;
import FinTwin.EGR.demo.exception.RecursoNaoEncontradoException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Service
public class OpenFinanceSyncService {

    private final ContaBancariaRepository contaBancariaRepository;
    private final UsuarioRepository usuarioRepository;
    
    
    public OpenFinanceSyncService(ContaBancariaRepository contaBancariaRepository,
                                  UsuarioRepository usuarioRepository) {
		this.contaBancariaRepository = contaBancariaRepository;
		this.usuarioRepository = usuarioRepository;
	}

    @Transactional
    public ContaBancaria vincularConta(Long usuarioId, ContaBancariaDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        ContaBancaria conta = new ContaBancaria(
                usuario,
                dto.getInstituicaoFinanceira(),
                dto.getAgencia(),
                dto.getNumeroConta(),
                dto.getTipoConta(),
                BigDecimal.ZERO,
                dto.getOpenFinanceAccountId()
        );
        conta.setDataProximaSincronizacao(LocalDateTime.now());
        return contaBancariaRepository.save(conta);
    }

    @Transactional(readOnly = true)
    public List<ContaBancaria> listarContas(Long usuarioId) {
        return contaBancariaRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public ContaBancaria buscarConta(Long contaId) {
        return contaBancariaRepository.findById(contaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta bancária não encontrada."));
    }

    @Transactional
    public ContaBancaria sincronizarConta(Long contaId) {
        ContaBancaria conta = buscarConta(contaId);
        sincronizarConta(conta);
        return conta;
    }

    @Transactional
    public List<ContaBancaria> sincronizarTodasContas(Long usuarioId) {
        List<ContaBancaria> contas = contaBancariaRepository.findByUsuarioId(usuarioId);
        for (ContaBancaria conta : contas) {
            sincronizarConta(conta);
        }
        return contas;
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
