package hexafoot.dados;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável pela leitura dos arquivos CSV que descrevem o torneio.
 */
public class LeitorCSVTorneio {
    private static final String ARQUIVO_GRUPOS = "/data/info_torneio/grupos.csv";
    private static final String ARQUIVO_CALENDARIO_FASE_GRUPOS = "/data/info_torneio/calendario_fase_grupos.csv";

    public List<String[]> lerGrupos() {
        return lerArquivo(ARQUIVO_GRUPOS, 5);
    }

    public List<String[]> lerCalendarioFaseGrupos() {
        return lerArquivo(ARQUIVO_CALENDARIO_FASE_GRUPOS, 4);
    }

    private List<String[]> lerArquivo(String caminho, int quantidadeColunas) {
        InputStream arquivo = getClass().getResourceAsStream(caminho);
        if (arquivo == null) {
            throw new IllegalStateException("Não foi possível localizar o arquivo " + caminho);
        }

        List<String[]> linhas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(arquivo, StandardCharsets.UTF_8))) {
            reader.readLine();

            String linha;
            int numeroLinha = 1;
            while ((linha = reader.readLine()) != null) {
                numeroLinha++;
                if (linha.isBlank()) {
                    continue;
                }

                String[] campos = linha.split(",", -1);
                if (campos.length != quantidadeColunas) {
                    throw new IllegalStateException("Linha " + numeroLinha + " inválida no arquivo " + caminho);
                }

                for (int i = 0; i < campos.length; i++) {
                    campos[i] = campos[i].trim();
                    if (campos[i].isEmpty()) {
                        throw new IllegalStateException("Campo vazio na linha " + numeroLinha + " do arquivo " + caminho);
                    }
                }

                linhas.add(campos);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o arquivo " + caminho, e);
        }

        return linhas;
    }
}
