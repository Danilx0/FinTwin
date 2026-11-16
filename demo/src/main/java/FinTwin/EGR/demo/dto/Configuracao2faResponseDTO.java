package FinTwin.EGR.demo.dto;

public class Configuracao2faResponseDTO {

    private String segredo;
    private String otpauthUri;
    private String qrCodeDataUri;

    public Configuracao2faResponseDTO(String segredo, String otpauthUri, String qrCodeDataUri) {
        this.segredo = segredo;
        this.otpauthUri = otpauthUri;
        this.qrCodeDataUri = qrCodeDataUri;
    }

    public String getSegredo() {
        return segredo;
    }

    public String getOtpauthUri() {
        return otpauthUri;
    }

    public String getQrCodeDataUri() {
        return qrCodeDataUri;
    }
}
