package FinTwin.EGR.demo.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "historico_remapeamento_ia")
public class HistoricoRemapeamentoIA extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transacao_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Transacao transacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaTransacao categoriaAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaTransacao novaCategoria;

    @Column(length = 200)
    private String padraoTextoDescricao;

    
    public HistoricoRemapeamentoIA() {
        super();
    }

    
    public HistoricoRemapeamentoIA(Usuario usuario, Transacao transacao, CategoriaTransacao categoriaAnterior, 
                                  CategoriaTransacao novaCategoria, String padraoTextoDescricao) {
        this();
        this.usuario = usuario;
        this.transacao = transacao;
        this.categoriaAnterior = categoriaAnterior;
        this.novaCategoria = novaCategoria;
        this.padraoTextoDescricao = padraoTextoDescricao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Transacao getTransacao() {
        return transacao;
    }

    public void setTransacao(Transacao transacao) {
        this.transacao = transacao;
    }

    public CategoriaTransacao getCategoriaAnterior() {
        return categoriaAnterior;
    }

    public void setCategoriaAnterior(CategoriaTransacao categoriaAnterior) {
        this.categoriaAnterior = categoriaAnterior;
    }

    public CategoriaTransacao getNovaCategoria() {
        return novaCategoria;
    }

    public void setNovaCategoria(CategoriaTransacao novaCategoria) {
        this.novaCategoria = novaCategoria;
    }

    public String getPadraoTextoDescricao() {
        return padraoTextoDescricao;
    }

    public void setPadraoTextoDescricao(String padraoTextoDescricao) {
        this.padraoTextoDescricao = padraoTextoDescricao;
    }
}
