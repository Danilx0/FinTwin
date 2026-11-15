package FinTwin.EGR.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class Validacao2faRequestDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 6)
    private String codigoTotp;

    public Validacao2faRequestDTO() {
    }

    public Validacao2faRequestDTO(String email, String codigoTotp) {
        this.email = email;
        this.codigoTotp = codigoTotp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigoTotp() {
        return codigoTotp;
    }

    public void setCodigoTotp(String codigoTotp) {
        this.codigoTotp = codigoTotp;
    }
}
