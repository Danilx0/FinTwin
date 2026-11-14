package FinTwin.EGR.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Validacao2faRequestDTO {

    @NotBlank
    @Size(min = 6, max = 6)
    private String codigoTotp;

    public Validacao2faRequestDTO() {
    }

    public Validacao2faRequestDTO(String codigoTotp) {
        this.codigoTotp = codigoTotp;
    }

    public String getCodigoTotp() {
        return codigoTotp;
    }

    public void setCodigoTotp(String codigoTotp) {
        this.codigoTotp = codigoTotp;
    }
}
