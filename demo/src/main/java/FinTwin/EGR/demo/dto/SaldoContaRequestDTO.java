package FinTwin.EGR.demo.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Saldo informado pelo proprio usuario (sem integracao bancaria). */
public class SaldoContaRequestDTO {

    @NotNull(message = "Informe o saldo.")
    @Digits(integer = 13, fraction = 2, message = "Use no maximo 13 digitos e 2 casas decimais.")
    private BigDecimal saldo;

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
