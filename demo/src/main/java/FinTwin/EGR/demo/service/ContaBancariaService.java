package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.dto.ContaBancariaDTO;
import FinTwin.EGR.demo.exception.RecursoNaoEncontradoException;
import FinTwin.EGR.demo.exception.RegraNegocioException;
import FinTwin.EGR.demo.model.ContaBancaria;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.TipoContaBancaria;
import FinTwin.EGR.demo.repository.ContaBancariaRepository;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Contas bancarias do usuario. O saldo e informado manualmente (nao ha integracao bancaria). */
@Service
public class ContaBancariaService {

    private final ContaBancariaRepository contaBancariaRepository;
    private final UsuarioRepository usuarioRepository;

    public ContaBancariaService(ContaBancariaRepository contaBancariaRepository,
                               UsuarioRepository usuarioRepository) {
        this.contaBancariaRepository = contaBancariaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ContaBancaria vincularConta(Long usuarioId, ContaBancariaDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        BigDecimal saldoInicial = dto.getSaldo() != null ? dto.getSaldo() : BigDecimal.ZERO;
        validarSaldo(dto.getTipoConta(), saldoInicial);

        ContaBancaria conta = new ContaBancaria(
                usuario,
                dto.getInstituicaoFinanceira(),
                dto.getAgencia(),
                dto.getNumeroConta(),
                dto.getTipoConta(),
                saldoInicial
        );
        conta.setSaldoAtualizadoEm(LocalDateTime.now());
        return contaBancariaRepository.save(conta);
    }

    /** Saldo informado manualmente pelo usuario. */
    @Transactional
    public ContaBancaria atualizarSaldo(Long contaId, BigDecimal saldo) {
        ContaBancaria conta = buscarConta(contaId);
        validarSaldo(conta.getTipoConta(), saldo);
        conta.setSaldo(saldo);
        conta.setSaldoAtualizadoEm(LocalDateTime.now());
        return contaBancariaRepository.save(conta);
    }

    /** Remove a conta. As transacoes ligadas a ela continuam salvas, apenas sem conta (ON DELETE SET NULL). */
    @Transactional
    public void removerConta(Long contaId) {
        contaBancariaRepository.delete(buscarConta(contaId));
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

    /** So a conta corrente pode ficar negativa (cheque especial). */
    private void validarSaldo(TipoContaBancaria tipoConta, BigDecimal saldo) {
        if (saldo.signum() < 0 && tipoConta != TipoContaBancaria.CORRENTE) {
            throw new RegraNegocioException("Só a conta corrente pode ter saldo negativo.");
        }
    }
}
