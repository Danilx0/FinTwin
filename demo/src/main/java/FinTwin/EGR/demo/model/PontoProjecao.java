package FinTwin.EGR.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "pontos_projecao")
public class PontoProjecao extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gemeo_digital_id", nullable = false)
    private GemeoDigital gemeoDigital;

    @Column(nullable = false)
    private Integer ano; 

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal patrimonioProjetado;

    @Column(precision = 15, scale = 2)
    private BigDecimal rendimentoAcumulado;

    @Column(precision = 15, scale = 2)
    private BigDecimal aportesAcumulados;

    
    public PontoProjecao() {
        super();
    }

    
    public PontoProjecao(Integer ano, BigDecimal patrimonioProjetado, BigDecimal rendimentoAcumulado, BigDecimal aportesAcumulados) {
        this();
        this.ano = ano;
        this.patrimonioProjetado = patrimonioProjetado;
        this.rendimentoAcumulado = rendimentoAcumulado;
        this.aportesAcumulados = aportesAcumulados;
    }

    
    public PontoProjecao(GemeoDigital gemeoDigital, Integer ano, BigDecimal patrimonioProjetado, 
                         BigDecimal rendimentoAcumulado, BigDecimal aportesAcumulados) {
        this(ano, patrimonioProjetado, rendimentoAcumulado, aportesAcumulados);
        this.gemeoDigital = gemeoDigital;
    }

    public GemeoDigital getGemeoDigital() {
        return gemeoDigital;
    }

    public void setGemeoDigital(GemeoDigital gemeoDigital) {
        this.gemeoDigital = gemeoDigital;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public BigDecimal getPatrimonioProjetado() {
        return patrimonioProjetado;
    }

    public void setPatrimonioProjetado(BigDecimal patrimonioProjetado) {
        this.patrimonioProjetado = patrimonioProjetado;
    }

    public BigDecimal getRendimentoAcumulado() {
        return rendimentoAcumulado;
    }

    public void setRendimentoAcumulado(BigDecimal rendimentoAcumulado) {
        this.rendimentoAcumulado = rendimentoAcumulado;
    }

    public BigDecimal getAportesAcumulados() {
        return aportesAcumulados;
    }

    public void setAportesAcumulados(BigDecimal aportesAcumulados) {
        this.aportesAcumulados = aportesAcumulados;
    }
}
