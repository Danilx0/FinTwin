package FinTwin.EGR.demo.controller;

import FinTwin.EGR.demo.dto.LoginRequestDTO;
import FinTwin.EGR.demo.dto.LoginResponseDTO;
import FinTwin.EGR.demo.dto.RecuperarSenhaRequestDTO;
import FinTwin.EGR.demo.dto.RedefinirSenhaRequestDTO;
import FinTwin.EGR.demo.dto.Validacao2faRequestDTO;
import FinTwin.EGR.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO resposta = authService.autenticar(dto);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/validar-2fa")
    public ResponseEntity<LoginResponseDTO> validar2fa(@Valid @RequestBody Validacao2faRequestDTO dto) {
        LoginResponseDTO resposta = authService.validar2fa(dto);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<Map<String, String>> recuperarSenha(@Valid @RequestBody RecuperarSenhaRequestDTO dto) {
        String token = authService.solicitarRecuperacaoSenha(dto);
        return ResponseEntity.ok(Map.of(
                "mensagem", "Instrucoes de recuperacao de senha geradas com sucesso.",
                "tokenRecuperacao", token
        ));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequestDTO dto) {
        authService.redefinirSenha(dto);
        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso."));
    }
}
