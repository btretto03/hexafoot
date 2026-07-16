package hexafoot.dados;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeitorCSVTorneioTest {

    private final LeitorCSVTorneio leitor = new LeitorCSVTorneio();

    @Test
    void deveLerOsDozeGruposDoRecursoReal() {
        List<String[]> grupos = leitor.lerGrupos();

        assertEquals(12, grupos.size());
        assertTrue(grupos.stream().allMatch(campos -> campos.length == 5));
        assertArrayEquals(new String[] {"A", "México", "África do Sul", "Coreia do Sul", "República Tcheca"}, grupos.get(0));
        assertArrayEquals(new String[] {"L", "Inglaterra", "Croácia", "Gana", "Panamá"}, grupos.get(11));
    }

    @Test
    void deveLerAsSetentaEDuasPartidasDaFaseDeGrupos() {
        List<String[]> calendario = leitor.lerCalendarioFaseGrupos();

        assertEquals(72, calendario.size());
        assertTrue(calendario.stream().allMatch(campos -> campos.length == 4));
        assertArrayEquals(new String[] {"1", "A", "México", "África do Sul"}, calendario.get(0));
        assertArrayEquals(new String[] {"3", "L", "Gana", "Croácia"}, calendario.get(71));
    }

    @Test
    void deveLerAsTrintaEDuasPartidasDoMataMata() {
        List<String[]> gabarito = leitor.lerGabaritoMataMata();

        assertEquals(32, gabarito.size());
        assertTrue(gabarito.stream().allMatch(campos -> campos.length == 4));
        assertArrayEquals(new String[] {"DezesseisAvos", "M1", "1A", "3_1"}, gabarito.get(0));
        assertArrayEquals(new String[] {"Final", "M32", "Vencedor_M29", "Vencedor_M30"}, gabarito.get(31));
    }
}
