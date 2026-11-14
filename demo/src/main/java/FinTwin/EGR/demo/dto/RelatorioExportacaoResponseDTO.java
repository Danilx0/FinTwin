package FinTwin.EGR.demo.dto;

import FinTwin.EGR.demo.model.RelatorioExportacao;

import java.time.LocalDateTime;

public class RelatorioExportacaoResponseDTO {

    private Long id;
    private Long usuarioId;
    private String tipoRelatorio;
    private String formato;
    private Integer totalRegistros;
    private LocalDateTime dataGeracao;
    private String urlDownload;

    public RelatorioExportacaoResponseDTO() {
    }

    public RelatorioExportacaoResponseDTO(Long id, Long usuarioId, String tipoRelatorio, String formato,
                                          Integer totalRegistros, LocalDateTime dataGeracao, String urlDownload) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tipoRelatorio = tipoRelatorio;
        this.formato = formato;
        this.totalRegistros = totalRegistros;
        this.dataGeracao = dataGeracao;
        this.urlDownload = urlDownload;
    }

    public static RelatorioExportacaoResponseDTO fromEntity(RelatorioExportacao relatorio) {
        if (relatorio == null) {
            return null;
        }
        Long uId = relatorio.getUsuario() != null ? relatorio.getUsuario().getId() : null;

        return new RelatorioExportacaoResponseDTO(
                relatorio.getId(),
                uId,
                relatorio.getTipoRelatorio(),
                relatorio.getFormato(),
                relatorio.getTotalRegistros(),
                relatorio.getDataGeracao(),
                relatorio.getUrlDownload()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipoRelatorio() {
        return tipoRelatorio;
    }

    public void setTipoRelatorio(String tipoRelatorio) {
        this.tipoRelatorio = tipoRelatorio;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public Integer getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(Integer totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDateTime dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public String getUrlDownload() {
        return urlDownload;
    }

    public void setUrlDownload(String urlDownload) {
        this.urlDownload = urlDownload;
    }
}
