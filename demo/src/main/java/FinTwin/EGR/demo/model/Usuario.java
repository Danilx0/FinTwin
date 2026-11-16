package FinTwin.EGR.demo.model;

import FinTwin.EGR.demo.model.enums.TipoUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario extends BaseEntity {

    public static final String FUSO_HORARIO_PADRAO = "America/Sao_Paulo";

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String senha;

    private LocalDateTime fotoAtualizadaEm;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false, length = 50)
    private String fusoHorario = FUSO_HORARIO_PADRAO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoUsuario tipoUsuario;

    @Column(nullable = false)
    private Boolean doisFatoresAtivo = false;

    private String totpSecret;

    private String tokenRecuperacaoSenha;
    private LocalDateTime expiracaoTokenSenha;

    private Boolean solicitouEsquecimento = false;

    private LocalDateTime dataSolicitacaoEsquecimento;

    private LocalDateTime dataLimiteExclusao;

    
    public Usuario() {
        super();
        this.doisFatoresAtivo = false;
        this.solicitouEsquecimento = false;
        this.fusoHorario = FUSO_HORARIO_PADRAO;
    }

    
    public Usuario(String nome, String email, String senha, TipoUsuario tipoUsuario) {
        this();
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
    }

    public void solicitarDireitoAoEsquecimento() {
        this.solicitouEsquecimento = true;
        this.dataSolicitacaoEsquecimento = LocalDateTime.now();
        this.dataLimiteExclusao = this.dataSolicitacaoEsquecimento.plusHours(24);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDateTime getFotoAtualizadaEm() {
        return fotoAtualizadaEm;
    }

    public void setFotoAtualizadaEm(LocalDateTime fotoAtualizadaEm) {
        this.fotoAtualizadaEm = fotoAtualizadaEm;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getFusoHorario() {
        return fusoHorario;
    }

    public void setFusoHorario(String fusoHorario) {
        this.fusoHorario = fusoHorario;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public Boolean getDoisFatoresAtivo() {
        return doisFatoresAtivo;
    }

    public void setDoisFatoresAtivo(Boolean doisFatoresAtivo) {
        this.doisFatoresAtivo = doisFatoresAtivo;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public String getTokenRecuperacaoSenha() {
        return tokenRecuperacaoSenha;
    }

    public void setTokenRecuperacaoSenha(String tokenRecuperacaoSenha) {
        this.tokenRecuperacaoSenha = tokenRecuperacaoSenha;
    }

    public LocalDateTime getExpiracaoTokenSenha() {
        return expiracaoTokenSenha;
    }

    public void setExpiracaoTokenSenha(LocalDateTime expiracaoTokenSenha) {
        this.expiracaoTokenSenha = expiracaoTokenSenha;
    }

    public Boolean getSolicitouEsquecimento() {
        return solicitouEsquecimento;
    }

    public void setSolicitouEsquecimento(Boolean solicitouEsquecimento) {
        this.solicitouEsquecimento = solicitouEsquecimento;
    }

    public LocalDateTime getDataSolicitacaoEsquecimento() {
        return dataSolicitacaoEsquecimento;
    }

    public void setDataSolicitacaoEsquecimento(LocalDateTime dataSolicitacaoEsquecimento) {
        this.dataSolicitacaoEsquecimento = dataSolicitacaoEsquecimento;
    }

    public LocalDateTime getDataLimiteExclusao() {
        return dataLimiteExclusao;
    }

    public void setDataLimiteExclusao(LocalDateTime dataLimiteExclusao) {
        this.dataLimiteExclusao = dataLimiteExclusao;
    }
}
