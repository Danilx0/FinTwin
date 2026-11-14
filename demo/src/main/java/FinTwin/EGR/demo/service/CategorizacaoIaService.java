package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.model.HistoricoRemapeamentoIA;
import FinTwin.EGR.demo.model.Transacao;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import FinTwin.EGR.demo.repository.HistoricoRemapeamentoIARepository;
import FinTwin.EGR.demo.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CategorizacaoIaService {

    private final TransacaoRepository transacaoRepository;
    private final HistoricoRemapeamentoIARepository historicoRemapeamentoIARepository;

    public CategorizacaoIaService(TransacaoRepository transacaoRepository, 
                                  HistoricoRemapeamentoIARepository historicoRemapeamentoIARepository) {
        this.transacaoRepository = transacaoRepository;
        this.historicoRemapeamentoIARepository = historicoRemapeamentoIARepository;
    }

    
    public CategoriaTransacao sugerirCategoria(String descricao) {
        String texto = descricao.toLowerCase();
        if (texto.contains("mercado") || texto.contains("restaurante") || texto.contains("padaria") || texto.contains("ifood")) {
            return CategoriaTransacao.ALIMENTACAO;
        } else if (texto.contains("aluguel") || texto.contains("condominio") || texto.contains("energia") || texto.contains("agua")) {
            return CategoriaTransacao.MORADIA;
        } else if (texto.contains("uber") || texto.contains("gasolina") || texto.contains("posto") || texto.contains("onibus")) {
            return CategoriaTransacao.TRANSPORTE;
        } else if (texto.contains("farmacia") || texto.contains("medico") || texto.contains("hospital")) {
            return CategoriaTransacao.SAUDE;
        } else if (texto.contains("salario") || texto.contains("pro-labore") || texto.contains("ted recebida")) {
            return CategoriaTransacao.SALARIO;
        } else if (texto.contains("investimento") || texto.contains("cdb") || texto.contains("tesouro")) {
            return CategoriaTransacao.INVESTIMENTO;
        }
        return CategoriaTransacao.OUTROS;
    }

    
    @Transactional
    public Transacao remapearCategoria(Long transacaoId, CategoriaTransacao novaCategoria, String motivo) {
        Transacao transacao = transacaoRepository.findById(transacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada: " + transacaoId));

        CategoriaTransacao categoriaAnterior = transacao.getCategoria();
        transacao.setCategoria(novaCategoria);
        transacao.setRemapeadaManualmente(true);
        transacao.setMotivoRemapeamento(motivo);

        HistoricoRemapeamentoIA historico = new HistoricoRemapeamentoIA(
                transacao.getUsuario(),
                transacao,
                categoriaAnterior,
                novaCategoria,
                transacao.getDescricao()
        );

        historicoRemapeamentoIARepository.save(historico);
        return transacaoRepository.save(transacao);
    }

    
    public BigDecimal calcularTaxaPrecisaoIa(Usuario usuario) {
        long totalInteracoes = historicoRemapeamentoIARepository.countByUsuarioId(usuario.getId());
        long totalTransacoes = transacaoRepository.findByUsuarioId(usuario.getId()).size();

        if (totalTransacoes == 0) {
            return BigDecimal.ONE; 
        }

        if (totalInteracoes >= 100) {
            return BigDecimal.valueOf(0.95); 
        }

        double taxaAcerto = 1.0 - ((double) totalInteracoes / Math.max(totalTransacoes, 1));
        return BigDecimal.valueOf(Math.max(taxaAcerto, 0.70)).setScale(4, RoundingMode.HALF_UP);
    }
}
