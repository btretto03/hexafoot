package hexafoot.dados;

import hexafoot.model.FaseTorneio;
import hexafoot.model.Grupo;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.StatusPartidaTorneio;
import hexafoot.model.Time;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricaTorneioTest {

    private static List<Time> selecoes;

    private final FabricaTorneio fabrica = new FabricaTorneio();

    @BeforeAll
    static void carregarSelecoesReais() {
        FabricaSelecao fabricaSelecao = new FabricaSelecao();
        List<Time> carregadas = new ArrayList<>(fabricaSelecao.processarListasInternacionais());
        carregadas.add(fabricaSelecao.montarTime("brasil", fabricaSelecao.processarListaBrasil()));
        selecoes = List.copyOf(carregadas);
    }

    @Test
    void deveMontarDozeGruposComQuatroSelecoes() {
        List<Grupo> grupos = fabrica.montarGrupos(selecoes);
        Grupo grupoC = buscarGrupo(grupos, "C");

        assertEquals(12, grupos.size());
        assertTrue(grupos.stream().allMatch(grupo -> grupo.getTimes().size() == 4));
        assertEquals(48, grupos.stream().flatMap(grupo -> grupo.getTimes().stream()).map(Time::getNome).distinct().count());
        assertEquals(List.of("brasil", "marrocos", "haiti", "escocia"),
                grupoC.getTimes().stream().map(Time::getNome).toList());
        assertThrows(UnsupportedOperationException.class, () -> grupos.add(grupoC));
    }

    @Test
    void deveMontarCalendarioCompletoComIdsDeterministicos() {
        List<Grupo> grupos = fabrica.montarGrupos(selecoes);
        List<PartidaTorneio> partidas = fabrica.montarCalendarioFaseGrupos(grupos);
        PartidaTorneio primeira = buscarPartida(partidas, "FG-A-R1-J1");
        PartidaTorneio ultima = buscarPartida(partidas, "FG-L-R3-J2");

        assertEquals(72, partidas.size());
        assertEquals(72, partidas.stream().map(PartidaTorneio::getId).distinct().count());
        assertTrue(grupos.stream().allMatch(grupo -> partidas.stream()
                .filter(partida -> partida.getGrupo() == grupo)
                .count() == 6));
        assertEquals(FaseTorneio.FASE_DE_GRUPOS, primeira.getFase());
        assertEquals(1, primeira.getRodada());
        assertEquals("mexico", primeira.getMandante().getNome());
        assertEquals("africa_do_sul", primeira.getVisitante().getNome());
        assertSame(buscarGrupo(grupos, "A"), primeira.getGrupo());
        assertEquals(3, ultima.getRodada());
        assertEquals("gana", ultima.getMandante().getNome());
        assertEquals("croacia", ultima.getVisitante().getNome());
        assertThrows(UnsupportedOperationException.class, () -> partidas.remove(0));
    }

    @Test
    void deveMontarTodasAsFasesDoMataMataSemParticipantes() {
        List<PartidaTorneio> partidas = fabrica.montarChaveamentoMataMata();
        PartidaTorneio primeira = buscarPartida(partidas, "M1");
        PartidaTorneio terceiroLugar = buscarPartida(partidas, "M31");
        PartidaTorneio finalDaCopa = buscarPartida(partidas, "M32");

        assertEquals(32, partidas.size());
        assertEquals(16, quantidadeNaFase(partidas, FaseTorneio.DEZESSEIS_AVOS));
        assertEquals(8, quantidadeNaFase(partidas, FaseTorneio.OITAVAS));
        assertEquals(4, quantidadeNaFase(partidas, FaseTorneio.QUARTAS));
        assertEquals(2, quantidadeNaFase(partidas, FaseTorneio.SEMIFINAL));
        assertEquals(1, quantidadeNaFase(partidas, FaseTorneio.TERCEIRO_LUGAR));
        assertEquals(1, quantidadeNaFase(partidas, FaseTorneio.FINAL));
        assertEquals("1A", primeira.getIdentificadorOrigemMandante());
        assertEquals("3_1", primeira.getIdentificadorOrigemVisitante());
        assertEquals("Perdedor_M29", terceiroLugar.getIdentificadorOrigemMandante());
        assertEquals("Vencedor_M30", finalDaCopa.getIdentificadorOrigemVisitante());
        assertEquals(StatusPartidaTorneio.AGENDADA, primeira.getStatus());
        assertNull(primeira.getMandante());
        assertNull(primeira.getVisitante());
    }

    private Grupo buscarGrupo(List<Grupo> grupos, String identificador) {
        return grupos.stream()
                .filter(grupo -> grupo.getIdentificador().equals(identificador))
                .findFirst()
                .orElseThrow();
    }

    private PartidaTorneio buscarPartida(List<PartidaTorneio> partidas, String id) {
        return partidas.stream()
                .filter(partida -> partida.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private long quantidadeNaFase(List<PartidaTorneio> partidas, FaseTorneio fase) {
        return partidas.stream().filter(partida -> partida.getFase() == fase).count();
    }

}
