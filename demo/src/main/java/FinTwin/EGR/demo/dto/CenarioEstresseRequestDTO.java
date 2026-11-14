package FinTwin.EGR.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class CenarioEstresseRequestDTO {

    @NotBlank
    private String titulo;

    @NotNull
    @PositiveOrZero
    private BigDecimal rendaMensalAtual;

    @NotNull
    @PositiveOrZero
    private BigDecimal despesaMensalAtual;

    @NotNull
    @PositiveOrZero
    private BigDecimal reservaEmergencia;

    @NotNull
    @Positive
    private BigDecimal percentualCorteRenda = BigDecimal.valueOf(50.0);

    @NotNull
    @Positive
    private Integer duracaoMeses = 12;

    public CenarioEstresseRequestDTO() {
    }

    public CenarioEstresseRequestDTO(String titulo, BigDecimal rendaMensalAtual, BigDecimal despesaMensalAtual,
                                     BigDecimal reservaEmergencia, BigDecimal percentualCorteRenda, Integer duracaoMeses) {
        this.titulo = titulo;
        this.rendaMensalAtual = rendaMensalAtual;
        this.despesaMensalAtual = despesaMensalAtual;
        this.reservaEmergencia = reservaEmergencia;
        if (percentualCorteRenda != null) {
            this.percentualCorteRenda = percentualCorteRenda;
        }
        if (duracaoMeses != null) {
            this.duracaoMeses = duracaoMeses;
        }
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public BigDecimal getRendaMensalAtual() {
        return rendaMensalAtual;
    }

    public void setRendaMensalAtual(BigDecimal rendaMensalAtual) {
        this.rendaMensalAtual = rendaMensalAtual;
    }

    public BigDecimal getDespesaMensalAtual() {
        return despesaMensalAtual;
    }

    public void setDespesaMensalAtual(BigDecimal despesaMensalAtual) {
        this.despesaMensalAtual = despesaMensalAtual;
    }

    public BigDecimal getReservaEmergencia() {
        return reservaEmergencia;
    }

    public void setReservaEmergencia(BigDecimal reservaEmergencia) {
        this.reservaEmergencia = reservaEmergencia;
    }

    public BigDecimal getPercentualCorteRenda() {
        return percentualCorteRenda;
    }

    public void setPercentualCorteRenda(BigDecimal percentualCorteRenda) {
        this.percentualCorteRenda = percentualCorteRenda;
    }

    public Integer getDuracaoMeses() {
        return duracaoMeses;
    }

    public void setDuracaoMeses(Integer duracaoMeses) {
        this.duracaoMeses = duracaoMeses;
    }
}
