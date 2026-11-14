package FinTwin.EGR.demo.dto;

import FinTwin.EGR.demo.model.CenarioEstresse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CenarioEstresseResponseDTO {

    private Long id;
    private Long usuarioId;
    private String tituloCenario;
    private String descricao;
    private BigDecimal percentualReducaoRenda;
    private BigDecimal percentualAumentoDespesas;
    private Integer duracaoMeses;
    private BigDecimal impactoPatrimonialEstimado;
    private Integer mesesSobrevivenciaReserva;
    private LocalDateTime dataCriacao;

    public CenarioEstresseResponseDTO() {
    }

    public CenarioEstresseResponseDTO(Long id, Long usuarioId, String tituloCenario, String descricao,
                                      BigDecimal percentualReducaoRenda, BigDecimal percentualAumentoDespesas,
                                      Integer duracaoMeses, BigDecimal impactoPatrimonialEstimado,
                                      Integer mesesSobrevivenciaReserva, LocalDateTime dataCriacao) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tituloCenario = tituloCenario;
        this.descricao = descricao;
        this.percentualReducaoRenda = percentualReducaoRenda;
        this.percentualAumentoDespesas = percentualAumentoDespesas;
        this.duracaoMeses = duracaoMeses;
        this.impactoPatrimonialEstimado = impactoPatrimonialEstimado;
        this.mesesSobrevivenciaReserva = mesesSobrevivenciaReserva;
        this.dataCriacao = dataCriacao;
    }

    public static CenarioEstresseResponseDTO fromEntity(CenarioEstresse cenario) {
        if (cenario == null) {
            return null;
        }
        Long uId = cenario.getUsuario() != null ? cenario.getUsuario().getId() : null;

        return new CenarioEstresseResponseDTO(
                cenario.getId(),
                uId,
                cenario.getTituloCenario(),
                cenario.getDescricao(),
                cenario.getPercentualReducaoRenda(),
                cenario.getPercentualAumentoDespesas(),
                cenario.getDuracaoMeses(),
                cenario.getImpactoPatrimonialEstimado(),
                cenario.getMesesSobrevivenciaReserva(),
                cenario.getDataCriacao()
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

    public String getTituloCenario() {
        return tituloCenario;
    }

    public void setTituloCenario(String tituloCenario) {
        this.tituloCenario = tituloCenario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPercentualReducaoRenda() {
        return percentualReducaoRenda;
    }

    public void setPercentualReducaoRenda(BigDecimal percentualReducaoRenda) {
        this.percentualReducaoRenda = percentualReducaoRenda;
    }

    public BigDecimal getPercentualAumentoDespesas() {
        return percentualAumentoDespesas;
    }

    public void setPercentualAumentoDespesas(BigDecimal percentualAumentoDespesas) {
        this.percentualAumentoDespesas = percentualAumentoDespesas;
    }

    public Integer getDuracaoMeses() {
        return duracaoMeses;
    }

    public void setDuracaoMeses(Integer duracaoMeses) {
        this.duracaoMeses = duracaoMeses;
    }

    public BigDecimal getImpactoPatrimonialEstimado() {
        return impactoPatrimonialEstimado;
    }

    public void setImpactoPatrimonialEstimado(BigDecimal impactoPatrimonialEstimado) {
        this.impactoPatrimonialEstimado = impactoPatrimonialEstimado;
    }

    public Integer getMesesSobrevivenciaReserva() {
        return mesesSobrevivenciaReserva;
    }

    public void setMesesSobrevivenciaReserva(Integer mesesSobrevivenciaReserva) {
        this.mesesSobrevivenciaReserva = mesesSobrevivenciaReserva;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
