package hexafoot.dados;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Lê os CSVs de elencos e fornece seus registros brutos à fábrica de seleções.
 */
public class LeitorCSVSelecao {

    private static final String PASTA_ELENCOS = "src/main/resources/data/elencos";

    //-----------------Leitura dos elencos-----------------
    /**
     * Lê os arquivos terminados em {@code .csv} na pasta de elencos relativa ao
     * diretório de execução. A ordem resultante é a fornecida pelo sistema de arquivos.
     *
     * @throws IllegalStateException se a pasta não puder ser localizada ou listada
     */
    public List<ArquivoCSVSelecao> listarArquivosDeSelecoes() {
        List<ArquivoCSVSelecao> arquivos = new ArrayList<>();

        File pasta = new File(PASTA_ELENCOS);
        File[] csvs = pasta.listFiles((dir, nome) -> nome.endsWith(".csv"));

        if (csvs == null) {
            throw new IllegalStateException("Nao foi possivel localizar a pasta de elencos em " + pasta.getAbsolutePath());
        }

        for (File csv : csvs) {
            String nomeArquivo = csv.getName().replace(".csv", "");
            List<String[]> linhas = lerConteudoCSV(csv.getPath());
            arquivos.add(new ArquivoCSVSelecao(nomeArquivo, linhas));
        }

        return arquivos;
    }

    /**
     * Lê um CSV simples, descartando o cabeçalho e linhas vazias. Vírgulas entre
     * aspas não são tratadas e campos individuais não são normalizados.
     *
     * @param caminho caminho relativo ou absoluto do arquivo
     * @return registros já lidos; em caso de erro, imprime a exceção e devolve a
     *         lista vazia ou parcial
     */
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
