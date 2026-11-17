package FinTwin.EGR.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/** Foto de perfil do usuario (UC05), ja recortada e reduzida pelo servidor. */
@Entity
@Table(name = "fotos_perfil")
public class FotoPerfil {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** O id da foto e o id do usuario; a foto e apagada junto com a conta (LGPD). */
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] conteudo;

    @Column(name = "tipo_conteudo", nullable = false, length = 30)
    private String tipoConteudo;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm;

    protected FotoPerfil() {
    }

    public FotoPerfil(Usuario usuario) {
        this.usuario = usuario;
    }

    public void substituir(byte[] conteudo, String tipoConteudo, LocalDateTime atualizadaEm) {
        this.conteudo = conteudo;
        this.tipoConteudo = tipoConteudo;
        this.atualizadaEm = atualizadaEm;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public byte[] getConteudo() {
        return conteudo;
    }

    public String getTipoConteudo() {
        return tipoConteudo;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }
}
