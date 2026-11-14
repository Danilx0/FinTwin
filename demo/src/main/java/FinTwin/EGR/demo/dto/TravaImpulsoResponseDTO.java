package FinTwin.EGR.demo.dto;

import FinTwin.EGR.demo.model.TravaImpulso;
import FinTwin.EGR.demo.model.enums.StatusTravaImpulso;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TravaImpulsoResponseDTO {

    private Long id;
    private Long usuarioId;
    private String itemDesejado;
    private BigDecimal valor;
    private String justificativa;
    private StatusTravaImpulso status;
    private LocalDateTime dataEntradaQuarentena;
    private LocalDateTime dataLiberacaoObrigatoria;
    private LocalDateTime dataDecisaoFinal;
    private Boolean podeSerLiberada;

    public TravaImpulsoResponseDTO() {
    }

    public TravaImpulsoResponseDTO(Long id, Long usuarioId, String itemDesejado, BigDecimal valor,
                                  String justificativa, StatusTravaImpulso status,
                                  LocalDateTime dataEntradaQuarentena, LocalDateTime dataLiberacaoObrigatoria,
                                  LocalDateTime dataDecisaoFinal, Boolean podeSerLiberada) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.itemDesejado = itemDesejado;
        this.valor = valor;
        this.justificativa = justificativa;
        this.status = status;
        this.dataEntradaQuarentena = dataEntradaQuarentena;
        this.dataLiberacaoObrigatoria = dataLiberacaoObrigatoria;
        this.dataDecisaoFinal = dataDecisaoFinal;
        this.podeSerLiberada = podeSerLiberada;
    }

    public static TravaImpulsoResponseDTO fromEntity(TravaImpulso trava) {
        if (trava == null) {
            return null;
        }
        Long uId = trava.getUsuario() != null ? trava.getUsuario().getId() : null;

        return new TravaImpulsoResponseDTO(
                trava.getId(),
                uId,
                trava.getItemDesejado(),
                trava.getValor(),
                trava.getJustificativa(),
                trava.getStatus(),
                trava.getDataEntradaQuarentena(),
                trava.getDataLiberacaoObrigatoria(),
                trava.getDataDecisaoFinal(),
                trava.podeSerLiberada()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
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

    public Boolean getPodeSerLiberada() {
        return podeSerLiberada;
    }

    public void setPodeSerLiberada(Boolean podeSerLiberada) {
        this.podeSerLiberada = podeSerLiberada;
    }
}
