package FinTwin.EGR.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RecuperarSenhaRequestDTO {

    @NotBlank
    @Email
    private String email;

    public RecuperarSenhaRequestDTO() {
    }

    public RecuperarSenhaRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
