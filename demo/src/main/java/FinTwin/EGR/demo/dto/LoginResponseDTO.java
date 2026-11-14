package FinTwin.EGR.demo.dto;

public class LoginResponseDTO {

    private String token;
    private String tipo;
    private UsuarioResponseDTO usuario;

    public LoginResponseDTO() {
        this.tipo = "Bearer";
    }

    public LoginResponseDTO(String token, UsuarioResponseDTO usuario) {
        this.token = token;
        this.tipo = "Bearer";
        this.usuario = usuario;
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
}
