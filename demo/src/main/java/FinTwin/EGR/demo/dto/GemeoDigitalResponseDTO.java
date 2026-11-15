package FinTwin.EGR.demo.dto;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import FinTwin.EGR.demo.model.GemeoDigital;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<PontoProjecaoDTO> pontosProjecao;
    private LocalDateTime dataCriacao;

    public GemeoDigitalResponseDTO() {
    }

    public GemeoDigitalResponseDTO(Long id, Long usuarioId, String nomeCenario, Integer horizonteAnos,
                                   BigDecimal patrimonioInicial, BigDecimal aporteMensalEstimado,
                                   BigDecimal taxaRendimentoAnual, BigDecimal inflacaoProjetadaAnual,
                                   Long tempoProcessamentoMs, List<PontoProjecaoDTO> pontosProjecao,
                                   LocalDateTime dataCriacao) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nomeCenario = nomeCenario;
        this.horizonteAnos = horizonteAnos;
        this.patrimonioInicial = patrimonioInicial;
        this.aporteMensalEstimado = aporteMensalEstimado;
        this.taxaRendimentoAnual = taxaRendimentoAnual;
        this.inflacaoProjetadaAnual = inflacaoProjetadaAnual;
        this.tempoProcessamentoMs = tempoProcessamentoMs;
        this.pontosProjecao = pontosProjecao;
        this.dataCriacao = dataCriacao;
    }

    public static GemeoDigitalResponseDTO fromEntity(GemeoDigital gemeo, JsonMapper jsonMapper) {
        if (gemeo == null) {
            return null;
        }
        Long uId = gemeo.getUsuario() != null ? gemeo.getUsuario().getId() : null;

        List<PontoProjecaoDTO> pontosProjecao;
        try {
            pontosProjecao = gemeo.getResultadoProjecaoJson() == null
                    ? List.of()
                    : List.of(jsonMapper.readValue(gemeo.getResultadoProjecaoJson(), PontoProjecaoDTO[].class));
        } catch (JacksonException e) {
            throw new IllegalStateException("O resultado salvo da projeção está inválido.", e);
        }

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
                pontosProjecao,
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

    public List<PontoProjecaoDTO> getPontosProjecao() {
        return pontosProjecao;
    }

    public void setPontosProjecao(List<PontoProjecaoDTO> pontosProjecao) {
        this.pontosProjecao = pontosProjecao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
