package hexafoot.dados;

import java.util.List;

/**
 * Conteúdo bruto de um CSV de elenco associado ao nome do país derivado do arquivo.
 */
public class ArquivoCSVSelecao {
    private String nomePais;
    private List<String[]> linhas;

    /**
     * @param nomePais nome do arquivo sem a extensão; {@code brasil} identifica a
     *                 seleção tratada pela convocação
     * @param linhas registros no formato
     *               {@code [nome, clube, posição, ataque, defesa, físico, estresse]}
     */
    public ArquivoCSVSelecao(String nomePais, List<String[]> linhas) {
        this.nomePais = nomePais;
        this.linhas = linhas;
    }

    /**
     * Verifica o identificador exato {@code brasil}, com distinção entre maiúsculas
     * e minúsculas.
     */
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
