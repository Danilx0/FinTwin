package FinTwin.EGR.demo;

import FinTwin.EGR.demo.dto.Configuracao2faResponseDTO;
import FinTwin.EGR.demo.dto.LoginRequestDTO;
import FinTwin.EGR.demo.dto.LoginResponseDTO;
import FinTwin.EGR.demo.dto.PerfilAtualizacaoRequestDTO;
import FinTwin.EGR.demo.dto.Validacao2faRequestDTO;
import FinTwin.EGR.demo.exception.RegraNegocioException;
import FinTwin.EGR.demo.model.Transacao;
import FinTwin.EGR.demo.model.TravaImpulso;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import FinTwin.EGR.demo.model.enums.StatusTravaImpulso;
import FinTwin.EGR.demo.model.enums.TipoTransacao;
import FinTwin.EGR.demo.model.enums.TipoUsuario;
import FinTwin.EGR.demo.dto.UsuarioResponseDTO;
import FinTwin.EGR.demo.repository.FotoPerfilRepository;
import FinTwin.EGR.demo.repository.TransacaoRepository;
import FinTwin.EGR.demo.repository.TravaImpulsoRepository;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import FinTwin.EGR.demo.security.TotpService;
import FinTwin.EGR.demo.service.AuthService;
import FinTwin.EGR.demo.service.FotoPerfilService;
import FinTwin.EGR.demo.service.PerfilUsuarioService;
import FinTwin.EGR.demo.service.SegurancaLgpdService;
import FinTwin.EGR.demo.service.TravaImpulsoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FuncionalidadesPendentesTest {

    private static final String SENHA = "senha-de-teste-123";

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TransacaoRepository transacaoRepository;
    @Autowired private TravaImpulsoRepository travaImpulsoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PerfilUsuarioService perfilUsuarioService;
    @Autowired private AuthService authService;
    @Autowired private TotpService totpService;
    @Autowired private SegurancaLgpdService segurancaLgpdService;
    @Autowired private TravaImpulsoService travaImpulsoService;
    @Autowired private FotoPerfilService fotoPerfilService;
    @Autowired private FotoPerfilRepository fotoPerfilRepository;

    private Usuario novoUsuario() {
        String email = "teste-" + UUID.randomUUID() + "@fintwin.test";
        return usuarioRepository.save(new Usuario("Teste", email, passwordEncoder.encode(SENHA), TipoUsuario.INDIVIDUAL));
    }

    // UC05
    @Test
    void atualizaPerfilComTelefoneEFusoHorario() {
        Usuario usuario = novoUsuario();
        assertThat(usuario.getFusoHorario()).isEqualTo("America/Sao_Paulo");

        PerfilAtualizacaoRequestDTO dto = new PerfilAtualizacaoRequestDTO();
        dto.setNome("  Maria Silva ");
        dto.setTelefone("(71) 99999-0000");
        dto.setFusoHorario("America/Bahia");
        perfilUsuarioService.atualizarPerfil(usuario.getId(), dto);

        Usuario salvo = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(salvo.getNome()).isEqualTo("Maria Silva");
        assertThat(salvo.getTelefone()).isEqualTo("(71) 99999-0000");
        assertThat(salvo.getFusoHorario()).isEqualTo("America/Bahia");
    }

    @Test
    void recusaFusoHorarioInexistente() {
        Usuario usuario = novoUsuario();
        PerfilAtualizacaoRequestDTO dto = new PerfilAtualizacaoRequestDTO();
        dto.setNome("Teste");
        dto.setFusoHorario("Marte/Base_Alfa");

        assertThatThrownBy(() -> perfilUsuarioService.atualizarPerfil(usuario.getId(), dto))
                .isInstanceOf(RegraNegocioException.class);
    }

    // UC03
    @Test
    void ativa2faEExigeCodigoNoLogin() {
        Usuario usuario = novoUsuario();

        Configuracao2faResponseDTO configuracao = perfilUsuarioService.iniciarConfiguracao2fa(usuario.getId());
        assertThat(configuracao.getOtpauthUri()).startsWith("otpauth://totp/FinTwin");
        assertThat(configuracao.getQrCodeDataUri()).startsWith("data:image/png;base64,");

        assertThatThrownBy(() -> perfilUsuarioService.ativar2fa(usuario.getId(), "000000"))
                .isInstanceOf(RegraNegocioException.class);

        perfilUsuarioService.ativar2fa(usuario.getId(), totpService.codigoAtual(configuracao.getSegredo()));
        assertThat(usuarioRepository.findById(usuario.getId()).orElseThrow().getDoisFatoresAtivo()).isTrue();

        LoginResponseDTO primeiraEtapa = authService.autenticar(new LoginRequestDTO(usuario.getEmail(), SENHA));
        assertThat(primeiraEtapa.isTwoFactorRequired()).isTrue();
        assertThat(primeiraEtapa.getToken()).isNull();

        LoginResponseDTO segundaEtapa = authService.validar2fa(
                new Validacao2faRequestDTO(usuario.getEmail(), totpService.codigoAtual(configuracao.getSegredo())));
        assertThat(segundaEtapa.getToken()).isNotBlank();
    }

    // LGPD - esquecimento em ate 24h
    @Test
    void purgaApagaUsuarioEDadosRelacionadosAposPrazo() {
        Usuario usuario = novoUsuario();
        Transacao transacao = transacaoRepository.save(new Transacao(usuario, "Mercado", new BigDecimal("10.00"),
                TipoTransacao.DESPESA, CategoriaTransacao.ALIMENTACAO));

        segurancaLgpdService.registrarPedidoEsquecimento(usuario.getId());
        assertThat(segurancaLgpdService.purgarDadosEsquecimentoLgpd()).isZero();
        assertThat(usuarioRepository.existsById(usuario.getId())).isTrue();

        Usuario pendente = usuarioRepository.findById(usuario.getId()).orElseThrow();
        pendente.setDataLimiteExclusao(LocalDateTime.now().minusMinutes(1));
        usuarioRepository.save(pendente);

        assertThat(segurancaLgpdService.purgarDadosEsquecimentoLgpd()).isEqualTo(1);
        assertThat(usuarioRepository.existsById(usuario.getId())).isFalse();
        assertThat(transacaoRepository.existsById(transacao.getId())).isFalse();
    }

    // UC10
    @Test
    void travaSoLiberaApos24hERegistraDespesa() {
        Usuario usuario = novoUsuario();
        TravaImpulso trava = travaImpulsoService.submeterAoModoQuarentena(usuario, "Fone", new BigDecimal("300.00"), null);

        assertThatThrownBy(() -> travaImpulsoService.liberarGasto(trava.getId()))
                .isInstanceOf(IllegalStateException.class);

        trava.setDataLiberacaoObrigatoria(LocalDateTime.now().minusMinutes(1));
        travaImpulsoRepository.save(trava);

        TravaImpulso liberada = travaImpulsoService.liberarGasto(trava.getId());
        assertThat(liberada.getStatus()).isEqualTo(StatusTravaImpulso.LIBERADA);
        assertThat(transacaoRepository.findByUsuarioId(usuario.getId()))
                .singleElement()
                .satisfies(t -> {
                    assertThat(t.getTipoTransacao()).isEqualTo(TipoTransacao.DESPESA);
                    assertThat(t.getValor()).isEqualByComparingTo("300.00");
                });

        assertThatThrownBy(() -> travaImpulsoService.cancelarGasto(trava.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void travaPodeSerCanceladaDuranteQuarentena() {
        Usuario usuario = novoUsuario();
        TravaImpulso trava = travaImpulsoService.submeterAoModoQuarentena(usuario, "Tenis", new BigDecimal("500.00"), "promo");

        TravaImpulso cancelada = travaImpulsoService.cancelarGasto(trava.getId());

        assertThat(cancelada.getStatus()).isEqualTo(StatusTravaImpulso.CANCELADA);
        assertThat(travaImpulsoService.listarPorUsuario(usuario.getId())).hasSize(1);
        assertThat(transacaoRepository.findByUsuarioId(usuario.getId())).isEmpty();
    }

    // UC05 - foto de perfil
    private static byte[] imagemPng(int largura, int altura) throws IOException {
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D grafico = imagem.createGraphics();
        grafico.setColor(Color.ORANGE);
        grafico.fillRect(0, 0, largura, altura);
        grafico.dispose();
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", saida);
        return saida.toByteArray();
    }

    @Test
    void salvaFotoRecortadaEmQuadradoComoJpeg() throws IOException {
        Usuario usuario = novoUsuario();

        Usuario atualizado = fotoPerfilService.salvar(usuario.getId(), imagemPng(1200, 800));

        assertThat(atualizado.getFotoAtualizadaEm()).isNotNull();
        assertThat(UsuarioResponseDTO.fromEntity(atualizado).getFotoPerfilUrl())
                .startsWith("/api/usuarios/" + usuario.getId() + "/foto?v=");

        byte[] gravada = fotoPerfilService.buscar(usuario.getId()).orElseThrow().getConteudo();
        BufferedImage lida = ImageIO.read(new ByteArrayInputStream(gravada));
        assertThat(lida.getWidth()).isEqualTo(320);
        assertThat(lida.getHeight()).isEqualTo(320);
        assertThat(gravada[0] & 0xFF).isEqualTo(0xFF);
        assertThat(gravada[1] & 0xFF).isEqualTo(0xD8);
    }

    @Test
    void recusaArquivoQueNaoEImagem() {
        Usuario usuario = novoUsuario();
        byte[] texto = "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> fotoPerfilService.salvar(usuario.getId(), texto))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(fotoPerfilRepository.existsById(usuario.getId())).isFalse();
    }

    @Test
    void recusaImagemCorrompida() throws IOException {
        Usuario usuario = novoUsuario();
        byte[] png = imagemPng(50, 50);
        byte[] cortada = java.util.Arrays.copyOf(png, 20);

        assertThatThrownBy(() -> fotoPerfilService.salvar(usuario.getId(), cortada))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void removeFotoEApagaJuntoComAConta() throws IOException {
        Usuario usuario = novoUsuario();
        fotoPerfilService.salvar(usuario.getId(), imagemPng(400, 400));

        Usuario semFoto = fotoPerfilService.remover(usuario.getId());
        assertThat(semFoto.getFotoAtualizadaEm()).isNull();
        assertThat(UsuarioResponseDTO.fromEntity(semFoto).getFotoPerfilUrl()).isNull();
        assertThat(fotoPerfilRepository.existsById(usuario.getId())).isFalse();

        fotoPerfilService.salvar(usuario.getId(), imagemPng(400, 400));
        Usuario pendente = usuarioRepository.findById(usuario.getId()).orElseThrow();
        pendente.solicitarDireitoAoEsquecimento();
        pendente.setDataLimiteExclusao(LocalDateTime.now().minusMinutes(1));
        usuarioRepository.save(pendente);
        segurancaLgpdService.purgarDadosEsquecimentoLgpd();

        assertThat(fotoPerfilRepository.existsById(usuario.getId())).isFalse();
    }
}
