package FinTwin.EGR.demo.controller;

import FinTwin.EGR.demo.dto.ContaBancariaDTO;
import FinTwin.EGR.demo.dto.SaldoContaRequestDTO;
import FinTwin.EGR.demo.model.ContaBancaria;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.service.ContaBancariaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/open-finance")
public class ContaBancariaController {

    private final ContaBancariaService contaBancariaService;

    public ContaBancariaController(ContaBancariaService contaBancariaService) {
        this.contaBancariaService = contaBancariaService;
    }

    @PostMapping("/contas/{usuarioId}")
    public ResponseEntity<ContaBancariaDTO> vincularConta(@PathVariable Long usuarioId,
                                                          @Valid @RequestBody ContaBancariaDTO dto,
                                                          @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ContaBancaria conta = contaBancariaService.vincularConta(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ContaBancariaDTO.fromEntity(conta));
    }

    @GetMapping("/contas/{usuarioId}")
    public ResponseEntity<List<ContaBancariaDTO>> listarContas(@PathVariable Long usuarioId,
                                                               @AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (!usuarioAutorizado(usuarioId, usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ContaBancariaDTO> contas = contaBancariaService.listarContas(usuarioId).stream()
                .map(ContaBancariaDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(contas);
    }

    @DeleteMapping("/contas/{contaId}")
    public ResponseEntity<Void> removerConta(@PathVariable Long contaId,
                                             @AuthenticationPrincipal Usuario usuarioAutenticado) {
        ContaBancaria conta = contaBancariaService.buscarConta(contaId);
        if (!usuarioAutorizado(conta.getUsuario().getId(), usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        contaBancariaService.removerConta(contaId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/contas/{contaId}/saldo")
    public ResponseEntity<ContaBancariaDTO> atualizarSaldo(@PathVariable Long contaId,
                                                           @Valid @RequestBody SaldoContaRequestDTO dto,
                                                           @AuthenticationPrincipal Usuario usuarioAutenticado) {
        ContaBancaria conta = contaBancariaService.buscarConta(contaId);
        if (!usuarioAutorizado(conta.getUsuario().getId(), usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        conta = contaBancariaService.atualizarSaldo(contaId, dto.getSaldo());
        return ResponseEntity.ok(ContaBancariaDTO.fromEntity(conta));
    }

    private boolean usuarioAutorizado(Long usuarioId, Usuario usuarioAutenticado) {
        return usuarioAutenticado != null && usuarioId.equals(usuarioAutenticado.getId());
    }
}
