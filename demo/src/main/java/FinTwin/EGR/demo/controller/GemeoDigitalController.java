package FinTwin.EGR.demo.controller;

import FinTwin.EGR.demo.dto.CenarioEstresseRequestDTO;
import FinTwin.EGR.demo.dto.CenarioEstresseResponseDTO;
import FinTwin.EGR.demo.dto.GemeoDigitalRequestDTO;
import FinTwin.EGR.demo.dto.GemeoDigitalResponseDTO;
import FinTwin.EGR.demo.model.CenarioEstresse;
import FinTwin.EGR.demo.model.GemeoDigital;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.service.GemeoDigitalService;
import FinTwin.EGR.demo.service.TesteEstresseService;
import tools.jackson.databind.json.JsonMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inteligencia")
public class GemeoDigitalController {

    private final GemeoDigitalService gemeoDigitalService;
    private final TesteEstresseService testeEstresseService;
    private final UsuarioRepository usuarioRepository;
    private final JsonMapper jsonMapper;

    public GemeoDigitalController(GemeoDigitalService gemeoDigitalService, 
                                  TesteEstresseService testeEstresseService, 
                                  UsuarioRepository usuarioRepository,
                                  JsonMapper jsonMapper) {
        this.gemeoDigitalService = gemeoDigitalService;
        this.testeEstresseService = testeEstresseService;
        this.usuarioRepository = usuarioRepository;
        this.jsonMapper = jsonMapper;
    }

    @PostMapping("/gemeo-digital/{usuarioId}")
    public ResponseEntity<GemeoDigitalResponseDTO> gerarGemeoDigital(@PathVariable Long usuarioId,
                                                                    @Valid @RequestBody GemeoDigitalRequestDTO dto,
                                                                    @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        GemeoDigital gemeo = gemeoDigitalService.projetarPatrimonio(
                usuario,
                dto.getNomeCenario(),
                dto.getHorizonteAnos(),
                dto.getPatrimonioInicial(),
                dto.getAporteMensal(),
                dto.getTaxaRendimentoAnual(),
                dto.getInflacaoAnual()
        );

        return ResponseEntity.ok(GemeoDigitalResponseDTO.fromEntity(gemeo, jsonMapper));
    }

    @GetMapping("/gemeo-digital/{usuarioId}")
    public ResponseEntity<List<GemeoDigitalResponseDTO>> listarGemeos(@PathVariable Long usuarioId,
                                                                       @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<GemeoDigitalResponseDTO> lista = gemeoDigitalService.listarPorUsuario(usuarioId).stream()
                .map(gemeo -> GemeoDigitalResponseDTO.fromEntity(gemeo, jsonMapper))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @DeleteMapping("/gemeo-digital/projecoes/{gemeoId}")
    public ResponseEntity<Void> excluirGemeo(@PathVariable Long gemeoId,
                                             @AuthenticationPrincipal Usuario usuarioAutenticado) {
        GemeoDigital gemeo = gemeoDigitalService.buscar(gemeoId);
        if (!usuarioAutorizado(gemeo.getUsuario().getId(), usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        gemeoDigitalService.excluir(gemeoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/teste-estresse/{usuarioId}")
    public ResponseEntity<CenarioEstresseResponseDTO> executarTesteEstresse(@PathVariable Long usuarioId,
                                                                           @Valid @RequestBody CenarioEstresseRequestDTO dto,
                                                                           @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        CenarioEstresse cenario = testeEstresseService.simularCriseRenda(
                usuario,
                dto.getTitulo(),
                dto.getRendaMensalAtual(),
                dto.getDespesaMensalAtual(),
                dto.getReservaEmergencia(),
                dto.getPercentualCorteRenda(),
                dto.getDuracaoMeses()
        );

        return ResponseEntity.ok(CenarioEstresseResponseDTO.fromEntity(cenario));
    }

    private boolean usuarioAutorizado(Long usuarioId, Usuario usuarioAutenticado) {
        return usuarioAutenticado != null && usuarioId.equals(usuarioAutenticado.getId());
    }
}
