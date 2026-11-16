package FinTwin.EGR.demo.service;

import FinTwin.EGR.demo.exception.RegraNegocioException;
import FinTwin.EGR.demo.model.FotoPerfil;
import FinTwin.EGR.demo.model.Usuario;
import FinTwin.EGR.demo.repository.FotoPerfilRepository;
import FinTwin.EGR.demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Optional;

/**
 * UC05 - foto de perfil enviada pelo usuario.
 * <p>
 * A imagem e sempre redesenhada no servidor: recorte quadrado central, 320x320, JPEG.
 * Isso descarta metadados (EXIF com localizacao, por exemplo) e qualquer conteudo
 * que nao seja a imagem em si.
 */
@Service
public class FotoPerfilService {

    public static final int TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024;
    static final int LADO_FINAL = 320;
    private static final long MAXIMO_PIXELS = 40_000_000L;
    private static final String TIPO_SAIDA = "image/jpeg";
    private static final Color FUNDO = new Color(0x11, 0x18, 0x27);

    private final FotoPerfilRepository fotoPerfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioService perfilUsuarioService;

    public FotoPerfilService(FotoPerfilRepository fotoPerfilRepository,
                             UsuarioRepository usuarioRepository,
                             PerfilUsuarioService perfilUsuarioService) {
        this.fotoPerfilRepository = fotoPerfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilUsuarioService = perfilUsuarioService;
    }

    @Transactional(readOnly = true)
    public Optional<FotoPerfil> buscar(Long usuarioId) {
        return fotoPerfilRepository.findById(usuarioId);
    }

    @Transactional
    public Usuario salvar(Long usuarioId, byte[] arquivo) {
        Usuario usuario = perfilUsuarioService.buscar(usuarioId);
        byte[] imagem = processar(arquivo);
        LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        FotoPerfil foto = fotoPerfilRepository.findById(usuarioId).orElseGet(() -> new FotoPerfil(usuarioId));
        foto.substituir(imagem, TIPO_SAIDA, agora);
        fotoPerfilRepository.save(foto);

        usuario.setFotoAtualizadaEm(agora);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario remover(Long usuarioId) {
        Usuario usuario = perfilUsuarioService.buscar(usuarioId);
        if (fotoPerfilRepository.existsById(usuarioId)) {
            fotoPerfilRepository.deleteById(usuarioId);
        }
        usuario.setFotoAtualizadaEm(null);
        return usuarioRepository.save(usuario);
    }

    byte[] processar(byte[] arquivo) {
        if (arquivo == null || arquivo.length == 0) {
            throw new RegraNegocioException("Selecione uma imagem.");
        }
        if (arquivo.length > TAMANHO_MAXIMO_BYTES) {
            throw new RegraNegocioException("A imagem passa de 5 MB.");
        }
        if (!ehJpeg(arquivo) && !ehPng(arquivo)) {
            throw new RegraNegocioException("Envie uma imagem JPG ou PNG.");
        }
        return gerarJpeg(recortarQuadrado(ler(arquivo)));
    }

    private BufferedImage ler(byte[] arquivo) {
        try (ImageInputStream entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(arquivo))) {
            Iterator<ImageReader> leitores = ImageIO.getImageReaders(entrada);
            if (!leitores.hasNext()) {
                throw new RegraNegocioException("Nao foi possivel ler a imagem.");
            }
            ImageReader leitor = leitores.next();
            try {
                leitor.setInput(entrada, true, true);
                // Confere as dimensoes antes de decodificar, para nao alocar memoria para imagens gigantes.
                long pixels = (long) leitor.getWidth(0) * leitor.getHeight(0);
                if (pixels > MAXIMO_PIXELS) {
                    throw new RegraNegocioException("A imagem e grande demais. Use uma foto de ate 40 megapixels.");
                }
                return leitor.read(0);
            } finally {
                leitor.dispose();
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof RegraNegocioException regra) {
                throw regra;
            }
            throw new RegraNegocioException("Nao foi possivel ler a imagem. Ela pode estar corrompida.");
        }
    }

    private BufferedImage recortarQuadrado(BufferedImage original) {
        int lado = Math.min(original.getWidth(), original.getHeight());
        int x = (original.getWidth() - lado) / 2;
        int y = (original.getHeight() - lado) / 2;
        BufferedImage fonte = reduzirEmEtapas(original.getSubimage(x, y, lado, lado));

        BufferedImage destino = new BufferedImage(LADO_FINAL, LADO_FINAL, BufferedImage.TYPE_INT_RGB);
        Graphics2D grafico = destino.createGraphics();
        try {
            aplicarQualidade(grafico);
            grafico.setColor(FUNDO);
            grafico.fillRect(0, 0, LADO_FINAL, LADO_FINAL);
            grafico.drawImage(fonte, 0, 0, LADO_FINAL, LADO_FINAL, null);
        } finally {
            grafico.dispose();
        }
        return destino;
    }

    /** Reduz pela metade ate chegar perto do tamanho final; evita serrilhado em fotos grandes. */
    private BufferedImage reduzirEmEtapas(BufferedImage imagem) {
        BufferedImage atual = imagem;
        while (atual.getWidth() / 2 >= LADO_FINAL) {
            int lado = atual.getWidth() / 2;
            BufferedImage menor = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
            Graphics2D grafico = menor.createGraphics();
            try {
                aplicarQualidade(grafico);
                grafico.drawImage(atual, 0, 0, lado, lado, null);
            } finally {
                grafico.dispose();
            }
            atual = menor;
        }
        return atual;
    }

    private void aplicarQualidade(Graphics2D grafico) {
        grafico.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        grafico.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private byte[] gerarJpeg(BufferedImage imagem) {
        ImageWriter escritor = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (ImageOutputStream destino = ImageIO.createImageOutputStream(saida)) {
            ImageWriteParam parametros = escritor.getDefaultWriteParam();
            parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parametros.setCompressionQuality(0.88f);
            escritor.setOutput(destino);
            escritor.write(null, new IIOImage(imagem, null, null), parametros);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar a foto de perfil.", e);
        } finally {
            escritor.dispose();
        }
        return saida.toByteArray();
    }

    private static boolean ehJpeg(byte[] dados) {
        return dados.length > 3 && (dados[0] & 0xFF) == 0xFF && (dados[1] & 0xFF) == 0xD8 && (dados[2] & 0xFF) == 0xFF;
    }

    private static boolean ehPng(byte[] dados) {
        return dados.length > 8 && (dados[0] & 0xFF) == 0x89 && dados[1] == 'P' && dados[2] == 'N' && dados[3] == 'G';
    }
}
