package FinTwin.EGR.demo.dto;

import FinTwin.EGR.demo.model.ContaBancaria;
import FinTwin.EGR.demo.model.enums.TipoContaBancaria;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ContaBancariaDTO {

    private Long id;
    private Long usuarioId;

    @NotBlank
    private String instituicaoFinanceira;

    private String agencia;
    private String numeroConta;

    @NotNull
    private TipoContaBancaria tipoConta;

    @Digits(integer = 13, fraction = 2, message = "Use no maximo 13 digitos e 2 casas decimais.")
    private BigDecimal saldo;

    private LocalDateTime saldoAtualizadoEm;

    public ContaBancariaDTO() {
    }

    public ContaBancariaDTO(Long id, Long usuarioId, String instituicaoFinanceira, String agencia,
                            String numeroConta, TipoContaBancaria tipoConta, BigDecimal saldo,
                            LocalDateTime saldoAtualizadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.instituicaoFinanceira = instituicaoFinanceira;
        this.agencia = agencia;
        this.numeroConta = numeroConta;
        this.tipoConta = tipoConta;
        this.saldo = saldo;
        this.saldoAtualizadoEm = saldoAtualizadoEm;
    }

    public static ContaBancariaDTO fromEntity(ContaBancaria conta) {
        if (conta == null) {
            return null;
        }
        Long uId = conta.getUsuario() != null ? conta.getUsuario().getId() : null;

        return new ContaBancariaDTO(
                conta.getId(),
                uId,
                conta.getInstituicaoFinanceira(),
                conta.getAgencia(),
                conta.getNumeroConta(),
                conta.getTipoConta(),
                conta.getSaldo(),
                conta.getSaldoAtualizadoEm()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getInstituicaoFinanceira() {
        return instituicaoFinanceira;
    }

    public void setInstituicaoFinanceira(String instituicaoFinanceira) {
        this.instituicaoFinanceira = instituicaoFinanceira;
    }

    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public String getNumeroConta() {
        return numeroConta;
    }

    public void setNumeroConta(String numeroConta) {
        this.numeroConta = numeroConta;
    }

    public TipoContaBancaria getTipoConta() {
        return tipoConta;
    }

    public void setTipoConta(TipoContaBancaria tipoConta) {
        this.tipoConta = tipoConta;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public LocalDateTime getSaldoAtualizadoEm() {
        return saldoAtualizadoEm;
    }

    public void setSaldoAtualizadoEm(LocalDateTime saldoAtualizadoEm) {
        this.saldoAtualizadoEm = saldoAtualizadoEm;
    }
}
