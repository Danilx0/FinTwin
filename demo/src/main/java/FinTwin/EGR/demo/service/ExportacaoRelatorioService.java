package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.model.RelatorioExportacao;
import FinTwin.EGR.demo.model.Transacao;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.RelatorioExportacaoRepository;
import FinTwin.EGR.demo.repository.TransacaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExportacaoRelatorioService {

    private static final int LIMITE_MAXIMO_REGISTROS = 5000;

    private final TransacaoRepository transacaoRepository;
    private final RelatorioExportacaoRepository relatorioExportacaoRepository;

    public ExportacaoRelatorioService(TransacaoRepository transacaoRepository, 
                                      RelatorioExportacaoRepository relatorioExportacaoRepository) {
        this.transacaoRepository = transacaoRepository;
        this.relatorioExportacaoRepository = relatorioExportacaoRepository;
    }

    @Transactional
    public RelatorioExportacao gerarRelatorioConsolidado(Usuario usuario, String formato) {

        List<Transacao> registros = transacaoRepository.findByUsuarioIdOrderByDataHoraDesc(
                usuario.getId(), 
                PageRequest.of(0, LIMITE_MAXIMO_REGISTROS)
        );

        RelatorioExportacao relatorio = new RelatorioExportacao(
                usuario, 
                "CONSOLIDADO", 
                formato.toUpperCase(), 
                registros.size()
        );
        relatorio.setUrlDownload("/relatorios/download/" + System.currentTimeMillis() + "." + formato.toLowerCase());

        return relatorioExportacaoRepository.save(relatorio);
    }
}
