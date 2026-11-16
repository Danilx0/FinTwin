package FinTwin.EGR.demo.controller;

import FinTwin.EGR.demo.dto.RelatorioExportacaoResponseDTO;
import FinTwin.EGR.demo.dto.TransacaoRequestDTO;
import FinTwin.EGR.demo.dto.TransacaoResponseDTO;
import FinTwin.EGR.demo.dto.TravaImpulsoRequestDTO;
import FinTwin.EGR.demo.dto.TravaImpulsoResponseDTO;
import FinTwin.EGR.demo.model.ContaBancaria;
import FinTwin.EGR.demo.model.RelatorioExportacao;
import FinTwin.EGR.demo.model.Transacao;
import FinTwin.EGR.demo.model.TravaImpulso;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import FinTwin.EGR.demo.repository.ContaBancariaRepository;
import FinTwin.EGR.demo.repository.TransacaoRepository;
import FinTwin.EGR.demo.repository.TravaImpulsoRepository;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.service.CategorizacaoIaService;
import FinTwin.EGR.demo.service.ExportacaoRelatorioService;
import FinTwin.EGR.demo.service.TravaImpulsoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/financas")
public class TransacaoController {

    private final TransacaoRepository transacaoRepository;
    private final TravaImpulsoRepository travaImpulsoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContaBancariaRepository contaBancariaRepository;
    private final CategorizacaoIaService categorizacaoIaService;
    private final TravaImpulsoService travaImpulsoService;
    private final ExportacaoRelatorioService exportacaoRelatorioService;

    public TransacaoController(TransacaoRepository transacaoRepository, 
                               TravaImpulsoRepository travaImpulsoRepository,
                               UsuarioRepository usuarioRepository,
                               ContaBancariaRepository contaBancariaRepository,
                               CategorizacaoIaService categorizacaoIaService, 
                               TravaImpulsoService travaImpulsoService, 
                               ExportacaoRelatorioService exportacaoRelatorioService) {
        this.transacaoRepository = transacaoRepository;
        this.travaImpulsoRepository = travaImpulsoRepository;
        this.usuarioRepository = usuarioRepository;
        this.contaBancariaRepository = contaBancariaRepository;
        this.categorizacaoIaService = categorizacaoIaService;
        this.travaImpulsoService = travaImpulsoService;
        this.exportacaoRelatorioService = exportacaoRelatorioService;
    }

    @GetMapping("/transacoes/{usuarioId}")
    public ResponseEntity<List<TransacaoResponseDTO>> listarTransacoes(@PathVariable Long usuarioId,
                                                                        @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<TransacaoResponseDTO> lista = transacaoRepository.findByUsuarioId(usuarioId).stream()
                .map(TransacaoResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/transacoes/{usuarioId}")
    public ResponseEntity<TransacaoResponseDTO> criarTransacao(@PathVariable Long usuarioId, 
                                                               @Valid @RequestBody TransacaoRequestDTO dto,
                                                               @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        ContaBancaria conta = null;
        if (dto.getContaBancariaId() != null) {
            conta = contaBancariaRepository.findById(dto.getContaBancariaId()).orElse(null);
            if (conta == null) {
                return ResponseEntity.notFound().build();
            }
            if (!usuarioId.equals(conta.getUsuario().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        LocalDateTime dataHora = dto.getDataHora() != null ? dto.getDataHora() : LocalDateTime.now();
        CategoriaTransacao sugerida = categorizacaoIaService.sugerirCategoria(dto.getDescricao());
        CategoriaTransacao categoriaFinal = dto.getCategoria() != null ? dto.getCategoria() : sugerida;

        Transacao transacao = new Transacao(
                usuario,
                conta,
                dto.getDescricao(),
                dto.getValor(),
                dataHora,
                dto.getTipoTransacao(),
                categoriaFinal
        );

        transacao.setCategoriaSugeridaIa(sugerida);
        transacao.setConfiancaIa(BigDecimal.valueOf(0.85));

        Transacao salva = transacaoRepository.save(transacao);
        return ResponseEntity.ok(TransacaoResponseDTO.fromEntity(salva));
    }

    @PutMapping("/transacoes/{transacaoId}/remapear")
    public ResponseEntity<TransacaoResponseDTO> remapearTransacao(@PathVariable Long transacaoId,
                                                                 @RequestParam CategoriaTransacao novaCategoria,
                                                                 @RequestParam(required = false) String motivo,
                                                                 @AuthenticationPrincipal Usuario usuarioAutenticado) {
        Transacao existente = transacaoRepository.findById(transacaoId).orElse(null);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        if (!usuarioAutorizado(existente.getUsuario().getId(), usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Transacao transacao = categorizacaoIaService.remapearCategoria(transacaoId, novaCategoria, motivo);
        return ResponseEntity.ok(TransacaoResponseDTO.fromEntity(transacao));
    }

    @PostMapping("/trava-impulso/{usuarioId}")
    public ResponseEntity<TravaImpulsoResponseDTO> criarTravaImpulso(@PathVariable Long usuarioId,
                                                                     @Valid @RequestBody TravaImpulsoRequestDTO dto,
                                                                     @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        TravaImpulso trava = travaImpulsoService.submeterAoModoQuarentena(
                usuario,
                dto.getItemDesejado(),
                dto.getValor(),
                dto.getJustificativa()
        );

        return ResponseEntity.ok(TravaImpulsoResponseDTO.fromEntity(trava));
    }

    @PutMapping("/trava-impulso/{travaId}/liberar")
    public ResponseEntity<?> liberarTrava(@PathVariable Long travaId,
                                          @AuthenticationPrincipal Usuario usuarioAutenticado) {
        TravaImpulso existente = travaImpulsoRepository.findById(travaId).orElse(null);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        if (!usuarioAutorizado(existente.getUsuario().getId(), usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            TravaImpulso trava = travaImpulsoService.liberarGasto(travaId);
            return ResponseEntity.ok(TravaImpulsoResponseDTO.fromEntity(trava));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/relatorios/exportar/{usuarioId}")
    public ResponseEntity<RelatorioExportacaoResponseDTO> exportarRelatorio(@PathVariable Long usuarioId,
                                                                           @RequestParam(defaultValue = "CSV") String formato,
                                                                           @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        RelatorioExportacao relatorio = exportacaoRelatorioService.gerarRelatorioConsolidado(usuario, formato);
        return ResponseEntity.ok(RelatorioExportacaoResponseDTO.fromEntity(relatorio));
    }

    private boolean usuarioAutorizado(Long usuarioId, Usuario usuarioAutenticado) {
        return usuarioAutenticado != null && usuarioId.equals(usuarioAutenticado.getId());
    }
}
