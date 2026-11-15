package FinTwin.EGR.demo.controller;

import FinTwin.EGR.demo.dto.ContaBancariaDTO;
import FinTwin.EGR.demo.model.ContaBancaria;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.service.OpenFinanceSyncService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/open-finance")
public class ContaBancariaController {

    private final OpenFinanceSyncService openFinanceSyncService;

    public ContaBancariaController(OpenFinanceSyncService openFinanceSyncService) {
        this.openFinanceSyncService = openFinanceSyncService;
    }

    @PostMapping("/contas/{usuarioId}")
    public ResponseEntity<ContaBancariaDTO> vincularConta(@PathVariable Long usuarioId,
                                                          @Valid @RequestBody ContaBancariaDTO dto,
                                                          @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ContaBancaria conta = openFinanceSyncService.vincularConta(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ContaBancariaDTO.fromEntity(conta));
    }

    @GetMapping("/contas/{usuarioId}")
    public ResponseEntity<List<ContaBancariaDTO>> listarContas(@PathVariable Long usuarioId,
                                                               @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ContaBancariaDTO> contas = openFinanceSyncService.listarContas(usuarioId).stream()
                .map(ContaBancariaDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(contas);
    }

    @PostMapping("/sincronizar/{contaId}")
    public ResponseEntity<ContaBancariaDTO> sincronizarConta(@PathVariable Long contaId,
                                                             @AuthenticationPrincipal Usuario usuarioAutenticado) {
        ContaBancaria conta = openFinanceSyncService.buscarConta(contaId);
        if (!usuarioAutorizado(conta.getUsuario().getId(), usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        conta = openFinanceSyncService.sincronizarConta(contaId);
        return ResponseEntity.ok(ContaBancariaDTO.fromEntity(conta));
    }

    @PostMapping("/sincronizar-todas/{usuarioId}")
    public ResponseEntity<List<ContaBancariaDTO>> sincronizarTodas(@PathVariable Long usuarioId,
                                                                  @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ContaBancariaDTO> contas = openFinanceSyncService.sincronizarTodasContas(usuarioId).stream()
                .map(ContaBancariaDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(contas);
    }

    private boolean usuarioAutorizado(Long usuarioId, Usuario usuarioAutenticado) {
        return usuarioAutenticado != null && usuarioId.equals(usuarioAutenticado.getId());
    }
}
