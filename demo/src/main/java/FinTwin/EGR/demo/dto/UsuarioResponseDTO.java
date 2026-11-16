package FinTwin.EGR.demo.dto;

import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.TipoUsuario;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private String fotoPerfilUrl;
    private String telefone;
    private String fusoHorario;
    private TipoUsuario tipoUsuario;
    private Boolean doisFatoresAtivo;
    private LocalDateTime dataCriacao;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long id, String nome, String email, String fotoPerfilUrl,
                              TipoUsuario tipoUsuario, Boolean doisFatoresAtivo, LocalDateTime dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.tipoUsuario = tipoUsuario;
        this.doisFatoresAtivo = doisFatoresAtivo;
        this.dataCriacao = dataCriacao;
    }

    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        UsuarioResponseDTO dto = new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                urlDaFoto(usuario),
                usuario.getTipoUsuario(),
                usuario.getDoisFatoresAtivo(),
                usuario.getDataCriacao()
        );
        dto.setTelefone(usuario.getTelefone());
        dto.setFusoHorario(usuario.getFusoHorario());
        return dto;
    }

    /** Endereco da foto com a versao na URL, para o navegador poder guardar em cache com seguranca. */
    private static String urlDaFoto(Usuario usuario) {
        if (usuario.getFotoAtualizadaEm() == null || usuario.getId() == null) {
            return null;
        }
        long versao = usuario.getFotoAtualizadaEm().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return "/api/usuarios/" + usuario.getId() + "/foto?v=" + versao;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
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

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
