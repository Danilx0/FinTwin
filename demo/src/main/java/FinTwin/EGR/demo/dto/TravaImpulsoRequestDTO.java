package FinTwin.EGR.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TravaImpulsoRequestDTO {

    @NotBlank
    @Size(max = 200)
    private String itemDesejado;

    @NotNull
    @Positive
    private BigDecimal valor;

    @Size(max = 500)
    private String justificativa;

    public TravaImpulsoRequestDTO() {
    }

    public TravaImpulsoRequestDTO(String itemDesejado, BigDecimal valor, String justificativa) {
        this.itemDesejado = itemDesejado;
        this.valor = valor;
        this.justificativa = justificativa;
    }

    public String getItemDesejado() {
        return itemDesejado;
    }

    public void setItemDesejado(String itemDesejado) {
        this.itemDesejado = itemDesejado;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }
}
