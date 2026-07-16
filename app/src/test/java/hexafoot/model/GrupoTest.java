package hexafoot.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrupoTest {

    @Test
    void deveNormalizarIdentificadorEPreservarOrdemDosTimes() {
        Time brasil = new Time("Brasil");
        Time japao = new Time("Japão");

        Grupo grupo = new Grupo(" c ", List.of(brasil, japao));

        assertEquals("C", grupo.getIdentificador());
        assertEquals(2, grupo.getTimes().size());
        assertSame(brasil, grupo.getTimes().get(0));
        assertSame(japao, grupo.getTimes().get(1));
    }

    @Test
    void deveCopiarListaRecebida() {
        Time brasil = new Time("Brasil");
        List<Time> origem = new ArrayList<>();
        origem.add(brasil);
        Grupo grupo = new Grupo("A", origem);

        origem.add(new Time("Japão"));

        assertEquals(1, grupo.getTimes().size());
        assertSame(brasil, grupo.getTimes().get(0));
    }

    @Test
    void deveExporListaImutavelEConsultarParticipacao() {
        Time brasil = new Time("Brasil");
        Time japao = new Time("Japão");
        Grupo grupo = new Grupo("A", List.of(brasil));

        assertTrue(grupo.contem(brasil));
        assertFalse(grupo.contem(japao));
        assertThrows(UnsupportedOperationException.class,
                        () -> grupo.getTimes().add(japao));
    }

    @Test
    void deveRejeitarListaNula() {
        assertThrows(NullPointerException.class, () -> new Grupo("A", null));
    }
}
