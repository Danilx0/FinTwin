package FinTwin.EGR.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "cenarios_estresse")
public class CenarioEstresse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String tituloCenario;

    @Column(length = 300)
    private String descricao;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualReducaoRenda = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentualAumentoDespesas = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer duracaoMeses;

    @Column(precision = 15, scale = 2)
    private BigDecimal impactoPatrimonialEstimado;

    private Integer mesesSobrevivenciaReserva;

    
    public CenarioEstresse() {
        super();
        this.percentualReducaoRenda = BigDecimal.ZERO;
        this.percentualAumentoDespesas = BigDecimal.ZERO;
    }

    
    public CenarioEstresse(Usuario usuario, String tituloCenario, BigDecimal percentualReducaoRenda, Integer duracaoMeses) {
        this();
        this.usuario = usuario;
        this.tituloCenario = tituloCenario;
        this.percentualReducaoRenda = percentualReducaoRenda;
        this.duracaoMeses = duracaoMeses;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTituloCenario() {
        return tituloCenario;
    }

    public void setTituloCenario(String tituloCenario) {
        this.tituloCenario = tituloCenario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPercentualReducaoRenda() {
        return percentualReducaoRenda;
    }

    public void setPercentualReducaoRenda(BigDecimal percentualReducaoRenda) {
        this.percentualReducaoRenda = percentualReducaoRenda;
    }

    public BigDecimal getPercentualAumentoDespesas() {
        return percentualAumentoDespesas;
    }

    public void setPercentualAumentoDespesas(BigDecimal percentualAumentoDespesas) {
        this.percentualAumentoDespesas = percentualAumentoDespesas;
    }

    public Integer getDuracaoMeses() {
        return duracaoMeses;
    }

    public void setDuracaoMeses(Integer duracaoMeses) {
        this.duracaoMeses = duracaoMeses;
    }

    public BigDecimal getImpactoPatrimonialEstimado() {
        return impactoPatrimonialEstimado;
    }

    public void setImpactoPatrimonialEstimado(BigDecimal impactoPatrimonialEstimado) {
        this.impactoPatrimonialEstimado = impactoPatrimonialEstimado;
    }

    public Integer getMesesSobrevivenciaReserva() {
        return mesesSobrevivenciaReserva;
    }

    public void setMesesSobrevivenciaReserva(Integer mesesSobrevivenciaReserva) {
        this.mesesSobrevivenciaReserva = mesesSobrevivenciaReserva;
    }
}
