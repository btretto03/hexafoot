package hexafoot.dados;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeitorCSVSelecaoTest {

    @TempDir
    Path pastaTemporaria;

    @Test
    void deveIgnorarCabecalhoELinhasVazias() throws IOException {
        Path arquivo = pastaTemporaria.resolve("selecao.csv");
        Files.writeString(arquivo,
                "Nome,Clube,Posicao,Ataque,Defesa,Fisico,Estresse\n"
                        + "\n"
                        + "Alisson,Liverpool,Goleiro,50,98,83,23\n"
                        + "   \n"
                        + "Marquinhos,PSG,Defensor,61,93,79,41\n",
                StandardCharsets.UTF_8);

        List<String[]> linhas = new LeitorCSVSelecao().lerConteudoCSV(arquivo.toString());

        assertEquals(2, linhas.size());
        assertArrayEquals(new String[] {"Alisson", "Liverpool", "Goleiro", "50", "98", "83", "23"}, linhas.get(0));
        assertArrayEquals(new String[] {"Marquinhos", "PSG", "Defensor", "61", "93", "79", "41"}, linhas.get(1));
    }

    @Test
    void devePreservarEspacosInternosDosCampos() throws IOException {
        Path arquivo = pastaTemporaria.resolve("campos.csv");
        Files.writeString(arquivo,
                "Nome,Clube,Posicao,Ataque,Defesa,Fisico,Estresse\n"
                        + "  Ana, Clube A,Meio-campista,70,65,88,12  \n",
                StandardCharsets.UTF_8);

        List<String[]> linhas = new LeitorCSVSelecao().lerConteudoCSV(arquivo.toString());

        assertEquals(1, linhas.size());
        assertEquals("Ana", linhas.get(0)[0]);
        assertEquals(" Clube A", linhas.get(0)[1]);
        assertEquals("12", linhas.get(0)[6]);
    }

    @Test
    void deveRetornarListaVaziaQuandoArquivoTemSomenteCabecalho() throws IOException {
        Path arquivo = pastaTemporaria.resolve("vazio.csv");
        Files.writeString(arquivo,
                "Nome,Clube,Posicao,Ataque,Defesa,Fisico,Estresse\n",
                StandardCharsets.UTF_8);

        List<String[]> linhas = new LeitorCSVSelecao().lerConteudoCSV(arquivo.toString());

        assertTrue(linhas.isEmpty());
    }
}
