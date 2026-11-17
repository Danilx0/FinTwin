package FinTwin.EGR.demo.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import FinTwin.EGR.demo.model.enums.StatusTravaImpulso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "travas_impulso")
public class TravaImpulso extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @Column(nullable = false, length = 200)
    private String itemDesejado;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(length = 500)
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTravaImpulso status = StatusTravaImpulso.QUARENTENA;

    @Column(nullable = false)
    private LocalDateTime dataEntradaQuarentena;

    @Column(nullable = false)
    private LocalDateTime dataLiberacaoObrigatoria;

    private LocalDateTime dataDecisaoFinal;

    
    public TravaImpulso() {
        super();
        this.status = StatusTravaImpulso.QUARENTENA;
    }

    
    public TravaImpulso(Usuario usuario, String itemDesejado, BigDecimal valor, String justificativa) {
        this();
        this.usuario = usuario;
        this.itemDesejado = itemDesejado;
        this.valor = valor;
        this.justificativa = justificativa;
        iniciarQuarentena();
    }

    public void iniciarQuarentena() {
        this.status = StatusTravaImpulso.QUARENTENA;
        this.dataEntradaQuarentena = LocalDateTime.now();
        this.dataLiberacaoObrigatoria = this.dataEntradaQuarentena.plusHours(24);
    }

    public boolean podeSerLiberada() {
        return LocalDateTime.now().isAfter(this.dataLiberacaoObrigatoria);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getItemDesejado() {
        return itemDesejado;
    }

    public void setItemDesejado(String itemDesejado) {
        this.itemDesejado = itemDesejado;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public StatusTravaImpulso getStatus() {
        return status;
    }

    public void setStatus(StatusTravaImpulso status) {
        this.status = status;
    }

    public LocalDateTime getDataEntradaQuarentena() {
        return dataEntradaQuarentena;
    }

    public void setDataEntradaQuarentena(LocalDateTime dataEntradaQuarentena) {
        this.dataEntradaQuarentena = dataEntradaQuarentena;
    }

    public LocalDateTime getDataLiberacaoObrigatoria() {
        return dataLiberacaoObrigatoria;
    }

    public void setDataLiberacaoObrigatoria(LocalDateTime dataLiberacaoObrigatoria) {
        this.dataLiberacaoObrigatoria = dataLiberacaoObrigatoria;
    }

    public LocalDateTime getDataDecisaoFinal() {
        return dataDecisaoFinal;
    }

    public void setDataDecisaoFinal(LocalDateTime dataDecisaoFinal) {
        this.dataDecisaoFinal = dataDecisaoFinal;
    }
}
