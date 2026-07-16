package hexafoot.dados;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArquivoCSVSelecaoTest {

    @Test
    void deveIdentificarApenasONomeExatoDoBrasil() {
        assertTrue(new ArquivoCSVSelecao("brasil", List.of()).isBrasil());
        assertFalse(new ArquivoCSVSelecao("Brasil", List.of()).isBrasil());
        assertFalse(new ArquivoCSVSelecao("brasil ", List.of()).isBrasil());
    }

    @Test
    void devePreservarAListaBrutaRecebida() {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[] {"Alisson", "Liverpool", "Goleiro", "50", "98", "83", "23"});

        ArquivoCSVSelecao arquivo = new ArquivoCSVSelecao("brasil", linhas);

        assertSame(linhas, arquivo.getLinhas());
        assertSame(linhas.get(0), arquivo.getLinhas().get(0));
    }
}
