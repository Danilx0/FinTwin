package FinTwin.EGR.demo.dto;

import java.math.BigDecimal;

public class PontoProjecaoDTO {

    private Integer ano;
    private BigDecimal patrimonioProjetado;
    private BigDecimal rendimentoAcumulado;
    private BigDecimal aportesAcumulados;

    public PontoProjecaoDTO() {
    }

    public PontoProjecaoDTO(Integer ano, BigDecimal patrimonioProjetado,
                            BigDecimal rendimentoAcumulado, BigDecimal aportesAcumulados) {
        this.ano = ano;
        this.patrimonioProjetado = patrimonioProjetado;
        this.rendimentoAcumulado = rendimentoAcumulado;
        this.aportesAcumulados = aportesAcumulados;
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
