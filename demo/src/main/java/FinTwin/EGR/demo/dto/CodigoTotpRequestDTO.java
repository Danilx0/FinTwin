package FinTwin.EGR.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CodigoTotpRequestDTO {

    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "O codigo deve ter 6 digitos.")
    private String codigoTotp;

    public CodigoTotpRequestDTO() {
    }

    public CodigoTotpRequestDTO(String codigoTotp) {
        this.codigoTotp = codigoTotp;
    }

    public String getCodigoTotp() {
        return codigoTotp;
    }

    public void setCodigoTotp(String codigoTotp) {
        this.codigoTotp = codigoTotp;
    }
}
