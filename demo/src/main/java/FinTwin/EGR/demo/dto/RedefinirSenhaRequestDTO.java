package FinTwin.EGR.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RedefinirSenhaRequestDTO {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 6, max = 100)
    private String novaSenha;

    public RedefinirSenhaRequestDTO() {
    }

    public RedefinirSenhaRequestDTO(String token, String novaSenha) {
        this.token = token;
        this.novaSenha = novaSenha;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}
