package hexafoot.dados;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que lê os arquivos .csv das seleções e retorna os dados brutos para a FabricaSelecao.
 */
public class LeitorCSVSelecao {

    private static final String PASTA_ELENCOS = "app/src/main/resources/data/elencos/";

    //-----------------Leitura dos elencos-----------------
    public List<ArquivoCSVSelecao> listarArquivosDeSelecoes() {
        List<ArquivoCSVSelecao> arquivos = new ArrayList<>();

        File pasta = new File(PASTA_ELENCOS);
        File[] csvs = pasta.listFiles();

        for (File csv : csvs) {
            String nomeArquivo = csv.getName().replace(".csv", "");
            List<String[]> linhas = lerConteudoCSV(csv.getPath());
            arquivos.add(new ArquivoCSVSelecao(nomeArquivo, linhas));
        }

        return arquivos;
    }

    public List<String[]> lerConteudoCSV(String caminho) {
        List<String[]> linhas = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(caminho));
            reader.readLine(); //pula o cabeçalho

            String linha;
            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty() == false) {
                    linhas.add(linha.split(","));
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return linhas;
    }
}
