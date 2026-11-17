package FinTwin.EGR.demo;

import FinTwin.EGR.demo.exception.RecursoNaoEncontradoException;
import FinTwin.EGR.demo.model.GemeoDigital;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.TipoUsuario;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.service.GemeoDigitalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ExclusaoGemeoDigitalTest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private GemeoDigitalService gemeoDigitalService;

    private GemeoDigital projetar(Usuario usuario, String nome) {
        return gemeoDigitalService.projetarPatrimonio(usuario, nome, 10, new BigDecimal("10000"),
                new BigDecimal("500"), new BigDecimal("10"), new BigDecimal("4"));
    }

    @Test
    void excluiSoAProjecaoEscolhida() {
        Usuario usuario = usuarioRepository.save(new Usuario("Teste", "gemeo-" + UUID.randomUUID() + "@fintwin.test",
                "hash", TipoUsuario.INDIVIDUAL));
        GemeoDigital manter = projetar(usuario, "Conservador");
        GemeoDigital excluir = projetar(usuario, "Arrojado");

        gemeoDigitalService.excluir(excluir.getId());

        assertThat(gemeoDigitalService.listarPorUsuario(usuario.getId()))
                .extracting(GemeoDigital::getId)
                .containsExactly(manter.getId());
        assertThatThrownBy(() -> gemeoDigitalService.excluir(excluir.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
