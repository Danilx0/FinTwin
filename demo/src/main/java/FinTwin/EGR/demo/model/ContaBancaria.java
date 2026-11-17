package FinTwin.EGR.demo.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import FinTwin.EGR.demo.model.enums.TipoContaBancaria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contas_bancarias")
public class ContaBancaria extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String instituicaoFinanceira;

    @Column(length = 20)
    private String agencia;

    @Column(length = 30)
    private String numeroConta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoContaBancaria tipoConta;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    /** Quando o usuario informou o saldo pela ultima vez. */
    private LocalDateTime saldoAtualizadoEm;

    
    public ContaBancaria() {
        super();
        this.saldo = BigDecimal.ZERO;
    }

    
    public ContaBancaria(Usuario usuario, String instituicaoFinanceira, TipoContaBancaria tipoConta, BigDecimal saldo) {
        this();
        this.usuario = usuario;
        this.instituicaoFinanceira = instituicaoFinanceira;
        this.tipoConta = tipoConta;
        this.saldo = saldo != null ? saldo : BigDecimal.ZERO;
    }

    
    public ContaBancaria(Usuario usuario, String instituicaoFinanceira, String agencia, String numeroConta, 
                         TipoContaBancaria tipoConta, BigDecimal saldo) {
        this(usuario, instituicaoFinanceira, tipoConta, saldo);
        this.agencia = agencia;
        this.numeroConta = numeroConta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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
