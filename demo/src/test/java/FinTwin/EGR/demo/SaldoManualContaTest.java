package FinTwin.EGR.demo;

import FinTwin.EGR.demo.dto.ContaBancariaDTO;
import FinTwin.EGR.demo.exception.RegraNegocioException;
import FinTwin.EGR.demo.model.ContaBancaria;
import FinTwin.EGR.demo.model.Transacao;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import FinTwin.EGR.demo.model.enums.TipoTransacao;
import FinTwin.EGR.demo.model.enums.TipoContaBancaria;
import FinTwin.EGR.demo.model.enums.TipoUsuario;
import FinTwin.EGR.demo.repository.ContaBancariaRepository;
import FinTwin.EGR.demo.repository.TransacaoRepository;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.service.ContaBancariaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SaldoManualContaTest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContaBancariaService contaBancariaService;
    @Autowired private ContaBancariaRepository contaBancariaRepository;
    @Autowired private TransacaoRepository transacaoRepository;

    private Usuario novoUsuario() {
        return usuarioRepository.save(new Usuario("Teste", "conta-" + UUID.randomUUID() + "@fintwin.test",
                "hash", TipoUsuario.INDIVIDUAL));
    }

    private ContaBancariaDTO dto(TipoContaBancaria tipo, String saldo) {
        ContaBancariaDTO dto = new ContaBancariaDTO();
        dto.setInstituicaoFinanceira("Banco Teste");
        dto.setTipoConta(tipo);
        dto.setSaldo(saldo == null ? null : new BigDecimal(saldo));
        return dto;
    }

    @Test
    void guardaSaldoInicialEADataEmQueFoiInformado() {
        ContaBancaria conta = contaBancariaService.vincularConta(novoUsuario().getId(), dto(TipoContaBancaria.CORRENTE, "1520.35"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("1520.35");
        assertThat(conta.getSaldoAtualizadoEm()).isNotNull();
    }

    @Test
    void contaSemSaldoInformadoComecaZerada() {
        ContaBancaria conta = contaBancariaService.vincularConta(novoUsuario().getId(), dto(TipoContaBancaria.POUPANCA, null));

        assertThat(conta.getSaldo()).isEqualByComparingTo("0");
    }

    @Test
    void atualizaSaldoEPermiteNegativoSoNaContaCorrente() {
        Long usuarioId = novoUsuario().getId();
        ContaBancaria corrente = contaBancariaService.vincularConta(usuarioId, dto(TipoContaBancaria.CORRENTE, "100"));
        ContaBancaria poupanca = contaBancariaService.vincularConta(usuarioId, dto(TipoContaBancaria.POUPANCA, "100"));

        ContaBancaria atualizada = contaBancariaService.atualizarSaldo(corrente.getId(), new BigDecimal("-250.00"));
        assertThat(atualizada.getSaldo()).isEqualByComparingTo("-250.00");
        assertThat(atualizada.getSaldoAtualizadoEm()).isAfterOrEqualTo(corrente.getSaldoAtualizadoEm());

        assertThatThrownBy(() -> contaBancariaService.atualizarSaldo(poupanca.getId(), new BigDecimal("-1")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("conta corrente");
        assertThatThrownBy(() -> contaBancariaService.vincularConta(usuarioId, dto(TipoContaBancaria.INVESTIMENTO, "-5")))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(contaBancariaService.buscarConta(poupanca.getId()).getSaldo()).isEqualByComparingTo("100");
    }

    @Test
    void removerContaMantemAsTransacoesSemConta() {
        Usuario usuario = novoUsuario();
        ContaBancaria conta = contaBancariaService.vincularConta(usuario.getId(), dto(TipoContaBancaria.CORRENTE, "300"));
        Transacao transacao = transacaoRepository.save(new Transacao(usuario, conta, "Mercado", new BigDecimal("80.00"),
                LocalDateTime.now(), TipoTransacao.DESPESA, CategoriaTransacao.ALIMENTACAO));

        contaBancariaService.removerConta(conta.getId());

        assertThat(contaBancariaRepository.findById(conta.getId())).isEmpty();
        Transacao mantida = transacaoRepository.findById(transacao.getId()).orElseThrow();
        assertThat(mantida.getContaBancaria()).isNull();
    }
}
