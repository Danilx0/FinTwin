package FinTwin.EGR.demo.controller;

import FinTwin.EGR.demo.model.RelatorioExportacao;
import FinTwin.EGR.demo.model.Transacao;
import FinTwin.EGR.demo.model.TravaImpulso;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import FinTwin.EGR.demo.repository.TransacaoRepository;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.service.CategorizacaoIaService;
import FinTwin.EGR.demo.service.ExportacaoRelatorioService;
import FinTwin.EGR.demo.service.TravaImpulsoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financas")
public class TransacaoController {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategorizacaoIaService categorizacaoIaService;
    private final TravaImpulsoService travaImpulsoService;
    private final ExportacaoRelatorioService exportacaoRelatorioService;

    public TransacaoController(TransacaoRepository transacaoRepository, 
                               UsuarioRepository usuarioRepository, 
                               CategorizacaoIaService categorizacaoIaService, 
                               TravaImpulsoService travaImpulsoService, 
                               ExportacaoRelatorioService exportacaoRelatorioService) {
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categorizacaoIaService = categorizacaoIaService;
        this.travaImpulsoService = travaImpulsoService;
        this.exportacaoRelatorioService = exportacaoRelatorioService;
    }

    @GetMapping("/transacoes/{usuarioId}")
    public ResponseEntity<List<Transacao>> listarTransacoes(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(transacaoRepository.findByUsuarioId(usuarioId));
    }

    @PostMapping("/transacoes/{usuarioId}")
    public ResponseEntity<Transacao> criarTransacao(@PathVariable Long usuarioId, @RequestBody Transacao transacao) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        transacao.setUsuario(usuario);
        CategoriaTransacao sugerida = categorizacaoIaService.sugerirCategoria(transacao.getDescricao());
        transacao.setCategoriaSugeridaIa(sugerida);

        if (transacao.getCategoria() == null) {
            transacao.setCategoria(sugerida);
        }

        transacao.setConfiancaIa(BigDecimal.valueOf(0.85));
        return ResponseEntity.ok(transacaoRepository.save(transacao));
    }

    @PutMapping("/transacoes/{transacaoId}/remapear")
    public ResponseEntity<Transacao> remapearTransacao(@PathVariable Long transacaoId,
                                                      @RequestParam CategoriaTransacao novaCategoria,
                                                      @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(categorizacaoIaService.remapearCategoria(transacaoId, novaCategoria, motivo));
    }

    @PostMapping("/trava-impulso/{usuarioId}")
    public ResponseEntity<TravaImpulso> criarTravaImpulso(@PathVariable Long usuarioId,
                                                          @RequestParam String item,
                                                          @RequestParam BigDecimal valor,
                                                          @RequestParam String justificativa) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return ResponseEntity.ok(travaImpulsoService.submeterAoModoQuarentena(usuario, item, valor, justificativa));
    }

    @PutMapping("/trava-impulso/{travaId}/liberar")
    public ResponseEntity<?> liberarTrava(@PathVariable Long travaId) {
        try {
            return ResponseEntity.ok(travaImpulsoService.liberarGasto(travaId));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/relatorios/exportar/{usuarioId}")
    public ResponseEntity<RelatorioExportacao> exportarRelatorio(@PathVariable Long usuarioId,
                                                                 @RequestParam(defaultValue = "CSV") String formato) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return ResponseEntity.ok(exportacaoRelatorioService.gerarRelatorioConsolidado(usuario, formato));
    }
}
