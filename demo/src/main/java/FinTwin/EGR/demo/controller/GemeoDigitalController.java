package FinTwin.EGR.demo.controller;

import FinTwin.EGR.demo.model.CenarioEstresse;
import FinTwin.EGR.demo.model.GemeoDigital;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.service.GemeoDigitalService;
import FinTwin.EGR.demo.service.TesteEstresseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inteligencia")
public class GemeoDigitalController {

    private final GemeoDigitalService gemeoDigitalService;
    private final TesteEstresseService testeEstresseService;
    private final UsuarioRepository usuarioRepository;

    public GemeoDigitalController(GemeoDigitalService gemeoDigitalService, 
                                  TesteEstresseService testeEstresseService, 
                                  UsuarioRepository usuarioRepository) {
        this.gemeoDigitalService = gemeoDigitalService;
        this.testeEstresseService = testeEstresseService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/gemeo-digital/{usuarioId}")
    public ResponseEntity<?> gerarGemeoDigital(@PathVariable Long usuarioId,
                                               @RequestParam String nomeCenario,
                                               @RequestParam(defaultValue = "35") int horizonteAnos,
                                               @RequestParam BigDecimal patrimonioInicial,
                                               @RequestParam BigDecimal aporteMensal,
                                               @RequestParam BigDecimal taxaRendimentoAnual,
                                               @RequestParam(defaultValue = "4.0") BigDecimal inflacaoAnual) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        GemeoDigital gemeo = gemeoDigitalService.projetarPatrimonio(
                usuario, nomeCenario, horizonteAnos, patrimonioInicial, aporteMensal, taxaRendimentoAnual, inflacaoAnual
        );

        return ResponseEntity.ok(gemeo);
    }

    @GetMapping("/gemeo-digital/{usuarioId}")
    public ResponseEntity<List<GemeoDigital>> listarGemeos(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(gemeoDigitalService.listarPorUsuario(usuarioId));
    }

    @PostMapping("/teste-estresse/{usuarioId}")
    public ResponseEntity<?> executarTesteEstresse(@PathVariable Long usuarioId,
                                                   @RequestParam String titulo,
                                                   @RequestParam BigDecimal rendaMensalAtual,
                                                   @RequestParam BigDecimal despesaMensalAtual,
                                                   @RequestParam BigDecimal reservaEmergencia,
                                                   @RequestParam(defaultValue = "50.0") BigDecimal percentualCorteRenda,
                                                   @RequestParam(defaultValue = "12") int duracaoMeses) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        CenarioEstresse cenario = testeEstresseService.simularCriseRenda(
                usuario, titulo, rendaMensalAtual, despesaMensalAtual, reservaEmergencia, percentualCorteRenda, duracaoMeses
        );

        return ResponseEntity.ok(cenario);
    }
}
