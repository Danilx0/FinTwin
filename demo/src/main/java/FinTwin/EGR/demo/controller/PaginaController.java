package FinTwin.EGR.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {

    @GetMapping({"/", "/login"})
    public String login() {
        return "fintwin-auth";
    }

    @GetMapping("/2fa")
    public String doisFatores() {
        return "fintwin-2fa";
    }

    @GetMapping("/recuperar-senha")
    public String recuperarSenha() {
        return "fintwin-recover";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "fintwin-dashboard";
    }

    @GetMapping("/gemeo-digital")
    public String gemeoDigital() {
        return "fintwin-gemeo";
    }

    @GetMapping("/transacoes")
    public String transacoes() {
        return "fintwin-transacoes";
    }

    @GetMapping("/teste-estresse")
    public String testeEstresse() {
        return "fintwin-estresse";
    }

    @GetMapping("/trava-impulso")
    public String travaImpulso() {
        return "fintwin-impulso";
    }

    @GetMapping("/relatorios")
    public String relatorios() {
        return "fintwin-relatorios";
    }
}
