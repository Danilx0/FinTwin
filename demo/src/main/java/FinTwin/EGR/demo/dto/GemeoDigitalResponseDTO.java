package FinTwin.EGR.demo.dto;

import FinTwin.EGR.demo.model.GemeoDigital;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GemeoDigitalResponseDTO {

    private Long id;
    private Long usuarioId;
    private String nomeCenario;
    private Integer horizonteAnos;
    private BigDecimal patrimonioInicial;
    private BigDecimal aporteMensalEstimado;
    private BigDecimal taxaRendimentoAnual;
    private BigDecimal inflacaoProjetadaAnual;
    private Long tempoProcessamentoMs;
    private LocalDateTime dataCriacao;

    public GemeoDigitalResponseDTO() {
    }

    public GemeoDigitalResponseDTO(Long id, Long usuarioId, String nomeCenario, Integer horizonteAnos,
                                   BigDecimal patrimonioInicial, BigDecimal aporteMensalEstimado,
                                   BigDecimal taxaRendimentoAnual, BigDecimal inflacaoProjetadaAnual,
                                   Long tempoProcessamentoMs, LocalDateTime dataCriacao) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nomeCenario = nomeCenario;
        this.horizonteAnos = horizonteAnos;
        this.patrimonioInicial = patrimonioInicial;
        this.aporteMensalEstimado = aporteMensalEstimado;
        this.taxaRendimentoAnual = taxaRendimentoAnual;
        this.inflacaoProjetadaAnual = inflacaoProjetadaAnual;
        this.tempoProcessamentoMs = tempoProcessamentoMs;
        this.dataCriacao = dataCriacao;
    }

    public static GemeoDigitalResponseDTO fromEntity(GemeoDigital gemeo) {
        if (gemeo == null) {
            return null;
        }
        Long uId = gemeo.getUsuario() != null ? gemeo.getUsuario().getId() : null;

        return new GemeoDigitalResponseDTO(
                gemeo.getId(),
                uId,
                gemeo.getNomeCenario(),
                gemeo.getHorizonteAnos(),
                gemeo.getPatrimonioInicial(),
                gemeo.getAporteMensalEstimado(),
                gemeo.getTaxaRendimentoAnual(),
                gemeo.getInflacaoProjetadaAnual(),
                gemeo.getTempoProcessamentoMs(),
                gemeo.getDataCriacao()
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

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
