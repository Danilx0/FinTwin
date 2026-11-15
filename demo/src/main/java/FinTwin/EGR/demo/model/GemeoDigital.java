package FinTwin.EGR.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "gemeos_digitais")
public class GemeoDigital extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String nomeCenario;

    @Column(nullable = false)
    private Integer horizonteAnos = 35;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal patrimonioInicial;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal aporteMensalEstimado;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxaRendimentoAnual;

    @Column(precision = 5, scale = 2)
    private BigDecimal inflacaoProjetadaAnual;

    private Long tempoProcessamentoMs;

    @Column(columnDefinition = "TEXT")
    private String resultadoProjecaoJson;

    
    public GemeoDigital() {
        super();
        this.horizonteAnos = 35;
    }

    
    public GemeoDigital(Usuario usuario, String nomeCenario, BigDecimal patrimonioInicial, 
                        BigDecimal aporteMensalEstimado, BigDecimal taxaRendimentoAnual) {
        this();
        this.usuario = usuario;
        this.nomeCenario = nomeCenario;
        this.patrimonioInicial = patrimonioInicial;
        this.aporteMensalEstimado = aporteMensalEstimado;
        this.taxaRendimentoAnual = taxaRendimentoAnual;
    }

    
    public GemeoDigital(Usuario usuario, String nomeCenario, Integer horizonteAnos, BigDecimal patrimonioInicial, 
                        BigDecimal aporteMensalEstimado, BigDecimal taxaRendimentoAnual, BigDecimal inflacaoProjetadaAnual) {
        this(usuario, nomeCenario, patrimonioInicial, aporteMensalEstimado, taxaRendimentoAnual);
        if (horizonteAnos != null) {
            this.horizonteAnos = Math.min(horizonteAnos, 35);
        }
        this.inflacaoProjetadaAnual = inflacaoProjetadaAnual;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNomeCenario() {
        return nomeCenario;
    }

    public void setNomeCenario(String nomeCenario) {
        this.nomeCenario = nomeCenario;
    }

    public Integer getHorizonteAnos() {
        return horizonteAnos;
    }

    public void setHorizonteAnos(Integer horizonteAnos) {
        this.horizonteAnos = horizonteAnos;
    }

    public BigDecimal getPatrimonioInicial() {
        return patrimonioInicial;
    }

    public void setPatrimonioInicial(BigDecimal patrimonioInicial) {
        this.patrimonioInicial = patrimonioInicial;
    }

    public BigDecimal getAporteMensalEstimado() {
        return aporteMensalEstimado;
    }

    public void setAporteMensalEstimado(BigDecimal aporteMensalEstimado) {
        this.aporteMensalEstimado = aporteMensalEstimado;
    }

    public BigDecimal getTaxaRendimentoAnual() {
        return taxaRendimentoAnual;
    }

    public void setTaxaRendimentoAnual(BigDecimal taxaRendimentoAnual) {
        this.taxaRendimentoAnual = taxaRendimentoAnual;
    }

    public BigDecimal getInflacaoProjetadaAnual() {
        return inflacaoProjetadaAnual;
    }

    public void setInflacaoProjetadaAnual(BigDecimal inflacaoProjetadaAnual) {
        this.inflacaoProjetadaAnual = inflacaoProjetadaAnual;
    }

    public Long getTempoProcessamentoMs() {
        return tempoProcessamentoMs;
    }

    public void setTempoProcessamentoMs(Long tempoProcessamentoMs) {
        this.tempoProcessamentoMs = tempoProcessamentoMs;
    }

    public String getResultadoProjecaoJson() {
        return resultadoProjecaoJson;
    }

    public void setResultadoProjecaoJson(String resultadoProjecaoJson) {
        this.resultadoProjecaoJson = resultadoProjecaoJson;
    }
}
