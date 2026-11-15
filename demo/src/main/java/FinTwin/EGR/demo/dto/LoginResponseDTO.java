package FinTwin.EGR.demo.dto;

public class LoginResponseDTO {

    private String token;
    private String tipo;
    private UsuarioResponseDTO usuario;
    private boolean twoFactorRequired;

    public LoginResponseDTO() {
        this.tipo = "Bearer";
    }

    public LoginResponseDTO(String token, UsuarioResponseDTO usuario) {
        this.token = token;
        this.tipo = "Bearer";
        this.usuario = usuario;
    }

    public LoginResponseDTO(boolean twoFactorRequired, UsuarioResponseDTO usuario) {
        this.tipo = "Bearer";
        this.usuario = usuario;
        this.twoFactorRequired = twoFactorRequired;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public UsuarioResponseDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponseDTO usuario) {
        this.usuario = usuario;
    }

    public boolean isTwoFactorRequired() {
        return twoFactorRequired;
    }

    public void setTwoFactorRequired(boolean twoFactorRequired) {
        this.twoFactorRequired = twoFactorRequired;
    }
}
