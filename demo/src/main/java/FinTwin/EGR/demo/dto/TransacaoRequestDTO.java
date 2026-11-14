package FinTwin.EGR.demo.dto;

import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import FinTwin.EGR.demo.model.enums.TipoTransacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransacaoRequestDTO {

    @NotBlank
    @Size(max = 200)
    private String descricao;

    @NotNull
    @Positive
    private BigDecimal valor;

    private LocalDateTime dataHora;

    @NotNull
    private TipoTransacao tipoTransacao;

    private CategoriaTransacao categoria;

    private Long contaBancariaId;

    public TransacaoRequestDTO() {
    }

    public TransacaoRequestDTO(String descricao, BigDecimal valor, LocalDateTime dataHora,
                               TipoTransacao tipoTransacao, CategoriaTransacao categoria, Long contaBancariaId) {
        this.descricao = descricao;
        this.valor = valor;
        this.dataHora = dataHora;
        this.tipoTransacao = tipoTransacao;
        this.categoria = categoria;
        this.contaBancariaId = contaBancariaId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public TipoTransacao getTipoTransacao() {
        return tipoTransacao;
    }

    public void setTipoTransacao(TipoTransacao tipoTransacao) {
        this.tipoTransacao = tipoTransacao;
    }

    public CategoriaTransacao getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaTransacao categoria) {
        this.categoria = categoria;
    }

    public Long getContaBancariaId() {
        return contaBancariaId;
    }

    public void setContaBancariaId(Long contaBancariaId) {
        this.contaBancariaId = contaBancariaId;
    }
}
