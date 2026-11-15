package FinTwin.EGR.demo.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErroRespostaDTO {

    private LocalDateTime timestamp;
    private Integer status;
    private String erro;
    private String mensagem;
    private String path;
    private Map<String, String> validacoes;

    public ErroRespostaDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public ErroRespostaDTO(Integer status, String erro, String mensagem, String path) {
        this();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.path = path;
    }

    public ErroRespostaDTO(Integer status, String erro, String mensagem, String path, Map<String, String> validacoes) {
        this(status, erro, mensagem, path);
        this.validacoes = validacoes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getValidacoes() {
        return validacoes;
    }

    public void setValidacoes(Map<String, String> validacoes) {
        this.validacoes = validacoes;
    }
}
