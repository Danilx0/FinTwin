package FinTwin.EGR.demo.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import FinTwin.EGR.demo.model.enums.TipoTransacao;
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
@Table(name = "transacoes")
public class Transacao extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private ContaBancaria contaBancaria;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransacao tipoTransacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaTransacao categoria;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CategoriaTransacao categoriaSugeridaIa;

    @Column(precision = 5, scale = 4)
    private BigDecimal confiancaIa;

    private Boolean remapeadaManualmente = false;

    private String motivoRemapeamento;

    
    public Transacao() {
        super();
        this.dataHora = LocalDateTime.now();
        this.remapeadaManualmente = false;
    }

    
    public Transacao(Usuario usuario, String descricao, BigDecimal valor, TipoTransacao tipoTransacao, CategoriaTransacao categoria) {
        this();
        this.usuario = usuario;
        this.descricao = descricao;
        this.valor = valor;
        this.tipoTransacao = tipoTransacao;
        this.categoria = categoria;
    }

    
    public Transacao(Usuario usuario, ContaBancaria contaBancaria, String descricao, BigDecimal valor, 
                     LocalDateTime dataHora, TipoTransacao tipoTransacao, CategoriaTransacao categoria) {
        this(usuario, descricao, valor, tipoTransacao, categoria);
        this.contaBancaria = contaBancaria;
        if (dataHora != null) {
            this.dataHora = dataHora;
        }
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ContaBancaria getContaBancaria() {
        return contaBancaria;
    }

    public void setContaBancaria(ContaBancaria contaBancaria) {
        this.contaBancaria = contaBancaria;
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

    public String getMotivoRemapeamento() {
        return motivoRemapeamento;
    }

    public void setMotivoRemapeamento(String motivoRemapeamento) {
        this.motivoRemapeamento = motivoRemapeamento;
    }
}
