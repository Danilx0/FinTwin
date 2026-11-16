package FinTwin.EGR.demo.security;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

/**
 * TOTP (RFC 6238): segredo Base32, codigos de 6 digitos, janela de 30 s com tolerancia de +-1 intervalo.
 */
@Service
public class TotpService {

    private static final String ALFABETO_BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final String EMISSOR = "FinTwin";
    private static final long INTERVALO_MS = 30_000L;

    private final SecureRandom random = new SecureRandom();

    public String gerarSegredo() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return codificarBase32(bytes);
    }

    public boolean validarCodigo(String segredo, String codigo) {
        if (segredo == null || segredo.isBlank() || codigo == null || !codigo.matches("\\d{6}")) {
            return false;
        }
        long intervaloAtual = System.currentTimeMillis() / INTERVALO_MS;
        try {
            for (long deslocamento = -1; deslocamento <= 1; deslocamento++) {
                String esperado = calcularCodigo(segredo, intervaloAtual + deslocamento);
                if (MessageDigest.isEqual(esperado.getBytes(StandardCharsets.US_ASCII),
                        codigo.getBytes(StandardCharsets.US_ASCII))) {
                    return true;
                }
            }
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            return false;
        }
        return false;
    }

    /** Codigo valido para o instante atual (usado em testes e diagnostico). */
    public String codigoAtual(String segredo) {
        try {
            return calcularCodigo(segredo, System.currentTimeMillis() / INTERVALO_MS);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao calcular codigo TOTP.", e);
        }
    }

    public String gerarOtpauthUri(String email, String segredo) {
        String rotulo = URLEncoder.encode(EMISSOR + ":" + email, StandardCharsets.UTF_8).replace("+", "%20");
        return "otpauth://totp/" + rotulo + "?secret=" + segredo + "&issuer=" + EMISSOR
                + "&algorithm=SHA1&digits=6&period=30";
    }

    public String gerarQrCodeDataUri(String conteudo) {
        try {
            BitMatrix matriz = new QRCodeWriter().encode(conteudo, BarcodeFormat.QR_CODE, 220, 220,
                    Map.of(EncodeHintType.MARGIN, 1));
            BufferedImage imagem = new BufferedImage(matriz.getWidth(), matriz.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < matriz.getWidth(); x++) {
                for (int y = 0; y < matriz.getHeight(); y++) {
                    imagem.setRGB(x, y, matriz.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }
            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            ImageIO.write(imagem, "png", saida);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(saida.toByteArray());
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Nao foi possivel gerar o QR Code do 2FA.", e);
        }
    }

    private String calcularCodigo(String segredo, long intervalo) throws GeneralSecurityException {
        byte[] chave = decodificarBase32(segredo);
        byte[] contador = ByteBuffer.allocate(Long.BYTES).putLong(intervalo).array();
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(chave, "HmacSHA1"));
        byte[] hash = mac.doFinal(contador);
        int indice = hash[hash.length - 1] & 0x0f;
        int valor = ((hash[indice] & 0x7f) << 24)
                | ((hash[indice + 1] & 0xff) << 16)
                | ((hash[indice + 2] & 0xff) << 8)
                | (hash[indice + 3] & 0xff);
        return String.format(Locale.ROOT, "%06d", valor % 1_000_000);
    }

    private String codificarBase32(byte[] dados) {
        StringBuilder resultado = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : dados) {
            buffer = (buffer << 8) | (b & 0xff);
            bits += 8;
            while (bits >= 5) {
                resultado.append(ALFABETO_BASE32.charAt((buffer >> (bits - 5)) & 0x1f));
                bits -= 5;
            }
        }
        if (bits > 0) {
            resultado.append(ALFABETO_BASE32.charAt((buffer << (5 - bits)) & 0x1f));
        }
        return resultado.toString();
    }

    private byte[] decodificarBase32(String segredo) {
        String normalizado = segredo.replace("=", "").replace(" ", "").toUpperCase(Locale.ROOT);
        byte[] resultado = new byte[normalizado.length() * 5 / 8];
        int buffer = 0;
        int bits = 0;
        int posicao = 0;
        for (char caractere : normalizado.toCharArray()) {
            int valor = ALFABETO_BASE32.indexOf(caractere);
            if (valor < 0) {
                throw new IllegalArgumentException("Segredo TOTP invalido.");
            }
            buffer = (buffer << 5) | valor;
            bits += 5;
            if (bits >= 8) {
                resultado[posicao++] = (byte) ((buffer >> (bits - 8)) & 0xff);
                bits -= 8;
            }
        }
        return Arrays.copyOf(resultado, posicao);
    }
}
