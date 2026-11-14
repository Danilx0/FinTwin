package FinTwin.EGR.demo.dto;

import FinTwin.EGR.demo.model.Transacao;
import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import FinTwin.EGR.demo.model.enums.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransacaoResponseDTO {

    private Long id;
    private Long usuarioId;
    private Long contaBancariaId;
    private String descricao;
    private BigDecimal valor;
    private LocalDateTime dataHora;
    private TipoTransacao tipoTransacao;
    private CategoriaTransacao categoria;
    private CategoriaTransacao categoriaSugeridaIa;
    private BigDecimal confiancaIa;
    private Boolean remapeadaManualmente;

    public TransacaoResponseDTO() {
    }

    public TransacaoResponseDTO(Long id, Long usuarioId, Long contaBancariaId, String descricao,
                                BigDecimal valor, LocalDateTime dataHora, TipoTransacao tipoTransacao,
                                CategoriaTransacao categoria, CategoriaTransacao categoriaSugeridaIa,
                                BigDecimal confiancaIa, Boolean remapeadaManualmente) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.contaBancariaId = contaBancariaId;
        this.descricao = descricao;
        this.valor = valor;
        this.dataHora = dataHora;
        this.tipoTransacao = tipoTransacao;
        this.categoria = categoria;
        this.categoriaSugeridaIa = categoriaSugeridaIa;
        this.confiancaIa = confiancaIa;
        this.remapeadaManualmente = remapeadaManualmente;
    }

    public static TransacaoResponseDTO fromEntity(Transacao transacao) {
        if (transacao == null) {
            return null;
        }
        Long contaId = transacao.getContaBancaria() != null ? transacao.getContaBancaria().getId() : null;
        Long uId = transacao.getUsuario() != null ? transacao.getUsuario().getId() : null;

        return new TransacaoResponseDTO(
                transacao.getId(),
                uId,
                contaId,
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getDataHora(),
                transacao.getTipoTransacao(),
                transacao.getCategoria(),
                transacao.getCategoriaSugeridaIa(),
                transacao.getConfiancaIa(),
                transacao.getRemapeadaManualmente()
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

    public Long getContaBancariaId() {
        return contaBancariaId;
    }

    public void setContaBancariaId(Long contaBancariaId) {
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

    public CategoriaTransacao getCategoriaSugeridaIa() {
        return categoriaSugeridaIa;
    }

    public void setCategoriaSugeridaIa(CategoriaTransacao categoriaSugeridaIa) {
        this.categoriaSugeridaIa = categoriaSugeridaIa;
    }

    public BigDecimal getConfiancaIa() {
        return confiancaIa;
    }

    public void setConfiancaIa(BigDecimal confiancaIa) {
        this.confiancaIa = confiancaIa;
    }

    public Boolean getRemapeadaManualmente() {
        return remapeadaManualmente;
    }

    public void setRemapeadaManualmente(Boolean remapeadaManualmente) {
        this.remapeadaManualmente = remapeadaManualmente;
    }
}
