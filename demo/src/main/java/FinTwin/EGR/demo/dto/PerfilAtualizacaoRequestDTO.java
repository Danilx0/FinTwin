package FinTwin.EGR.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PerfilAtualizacaoRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String nome;

    @Pattern(regexp = "^$|^\\+?[0-9 ()-]{8,20}$", message = "Telefone invalido. Use apenas numeros, espacos, parenteses, hifen e +.")
    private String telefone;

    @NotBlank
    @Size(max = 50)
    private String fusoHorario;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getFusoHorario() {
        return fusoHorario;
    }

    public void setFusoHorario(String fusoHorario) {
        this.fusoHorario = fusoHorario;
    }
}
