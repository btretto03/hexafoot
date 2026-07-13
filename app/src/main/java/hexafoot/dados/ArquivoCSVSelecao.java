package hexafoot.dados;

import java.util.List;

public class ArquivoCSVSelecao {
    private String nomePais;
    private List<String[]> linhas;

    public ArquivoCSVSelecao(String nomePais, List<String[]> linhas) {
        this.nomePais = nomePais;
        this.linhas = linhas;
    }

    public boolean isBrasil() {
        return this.nomePais.equals("brasil");
    }

    public String getNomePais() {
        return nomePais;
    }

    public List<String[]> getLinhas() {
        return linhas;
    }
}
