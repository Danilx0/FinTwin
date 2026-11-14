package FinTwin.EGR.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class GemeoDigitalRequestDTO {

    @NotBlank
    private String nomeCenario;

    @Min(1)
    @Max(35)
    private Integer horizonteAnos = 35;

    @NotNull
    @PositiveOrZero
    private BigDecimal patrimonioInicial;

    @NotNull
    @PositiveOrZero
    private BigDecimal aporteMensal;

    @NotNull
    private BigDecimal taxaRendimentoAnual;

    private BigDecimal inflacaoAnual = BigDecimal.valueOf(4.0);

    public GemeoDigitalRequestDTO() {
    }

    public GemeoDigitalRequestDTO(String nomeCenario, Integer horizonteAnos, BigDecimal patrimonioInicial,
                                  BigDecimal aporteMensal, BigDecimal taxaRendimentoAnual, BigDecimal inflacaoAnual) {
        this.nomeCenario = nomeCenario;
        if (horizonteAnos != null) {
            this.horizonteAnos = horizonteAnos;
        }
        this.patrimonioInicial = patrimonioInicial;
        this.aporteMensal = aporteMensal;
        this.taxaRendimentoAnual = taxaRendimentoAnual;
        if (inflacaoAnual != null) {
            this.inflacaoAnual = inflacaoAnual;
        }
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

    public BigDecimal getAporteMensal() {
        return aporteMensal;
    }

    public void setAporteMensal(BigDecimal aporteMensal) {
        this.aporteMensal = aporteMensal;
    }

    public BigDecimal getTaxaRendimentoAnual() {
        return taxaRendimentoAnual;
    }

    public void setTaxaRendimentoAnual(BigDecimal taxaRendimentoAnual) {
        this.taxaRendimentoAnual = taxaRendimentoAnual;
    }

    public BigDecimal getInflacaoAnual() {
        return inflacaoAnual;
    }

    public void setInflacaoAnual(BigDecimal inflacaoAnual) {
        this.inflacaoAnual = inflacaoAnual;
    }
}
