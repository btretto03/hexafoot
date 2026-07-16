package hexafoot.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PartidaTorneioTest {

    @Test
    void deveCriarPartidaDeGrupoComMetadadosEParticipantes() {
        Time mandante = new Time("Brasil");
        Time visitante = new Time("Japão");
        Grupo grupo = new Grupo(" c ", List.of(mandante, visitante));

        PartidaTorneio partida = PartidaTorneio.criarFaseDeGrupos(
                        " FG-C-R1-J1 ", 1, grupo, mandante, visitante);

        assertEquals("FG-C-R1-J1", partida.getId());
        assertEquals(FaseTorneio.FASE_DE_GRUPOS, partida.getFase());
        assertEquals(Integer.valueOf(1), partida.getRodada());
        assertSame(grupo, partida.getGrupo());
        assertEquals("Brasil", partida.getIdentificadorOrigemMandante());
        assertEquals("Japão", partida.getIdentificadorOrigemVisitante());
        assertSame(mandante, partida.getMandante());
        assertSame(visitante, partida.getVisitante());
        assertEquals(StatusPartidaTorneio.AGENDADA, partida.getStatus());
        assertNull(partida.getPartida());
        assertNull(partida.getVencedor());
        assertNull(partida.getPerdedor());
    }

    @Test
    void deveCriarEliminatoriaSemParticipantesDefinidos() {
        PartidaTorneio partida = PartidaTorneio.criarEliminatoria(
                        " M17 ", FaseTorneio.OITAVAS, "Vencedor_M1", "Vencedor_M2");

        assertEquals("M17", partida.getId());
        assertEquals(FaseTorneio.OITAVAS, partida.getFase());
        assertNull(partida.getRodada());
        assertNull(partida.getGrupo());
        assertEquals("Vencedor_M1", partida.getIdentificadorOrigemMandante());
        assertEquals("Vencedor_M2", partida.getIdentificadorOrigemVisitante());
        assertNull(partida.getMandante());
        assertNull(partida.getVisitante());
        assertEquals(StatusPartidaTorneio.AGENDADA, partida.getStatus());
    }

    @Test
    void deveDefinirParticipantesEmEtapas() {
        PartidaTorneio partida = PartidaTorneio.criarEliminatoria(
                        "M17", FaseTorneio.OITAVAS, "Vencedor_M1", "Vencedor_M2");
        Time mandante = new Time("Brasil");
        Time visitante = new Time("Japão");

        partida.definirParticipantes(mandante, null);
        assertSame(mandante, partida.getMandante());
        assertNull(partida.getVisitante());

        partida.definirParticipantes(mandante, visitante);
        assertSame(mandante, partida.getMandante());
        assertSame(visitante, partida.getVisitante());
    }

    @Test
    void deveIniciarEAssociarNovaPartida() {
        Time mandante = new Time("Brasil");
        Time visitante = new Time("Japão");
        PartidaTorneio agendamento = PartidaTorneio.criarEliminatoria(
                        "M17", FaseTorneio.OITAVAS, "Vencedor_M1", "Vencedor_M2");
        agendamento.definirParticipantes(mandante, visitante);

        Partida partida = agendamento.iniciar();

        assertSame(partida, agendamento.getPartida());
        assertSame(mandante, partida.getMandante());
        assertSame(visitante, partida.getVisitante());
        assertEquals(StatusPartidaTorneio.EM_ANDAMENTO, agendamento.getStatus());
    }

    @Test
    void deveConcluirPartidaDeGrupoSemDefinirVencedor() {
        Time mandante = new Time("Brasil");
        Time visitante = new Time("Japão");
        Grupo grupo = new Grupo("C", List.of(mandante, visitante));
        PartidaTorneio partida = PartidaTorneio.criarFaseDeGrupos(
                        "FG-C-R1-J1", 1, grupo, mandante, visitante);

        partida.iniciar();
        partida.concluir();

        assertEquals(StatusPartidaTorneio.CONCLUIDA, partida.getStatus());
        assertNull(partida.getVencedor());
        assertNull(partida.getPerdedor());
    }

    @Test
    void deveInferirPerdedorAoConcluirEliminatoria() {
        Time mandante = new Time("Brasil");
        Time visitante = new Time("Japão");
        PartidaTorneio vitoriaMandante = eliminatoriaCom(mandante, visitante);
        PartidaTorneio vitoriaVisitante = eliminatoriaCom(mandante, visitante);

        vitoriaMandante.concluir(mandante);
        vitoriaVisitante.concluir(visitante);

        assertEquals(StatusPartidaTorneio.CONCLUIDA, vitoriaMandante.getStatus());
        assertSame(mandante, vitoriaMandante.getVencedor());
        assertSame(visitante, vitoriaMandante.getPerdedor());
        assertEquals(StatusPartidaTorneio.CONCLUIDA, vitoriaVisitante.getStatus());
        assertSame(visitante, vitoriaVisitante.getVencedor());
        assertSame(mandante, vitoriaVisitante.getPerdedor());
    }

    private PartidaTorneio eliminatoriaCom(Time mandante, Time visitante) {
        PartidaTorneio partida = PartidaTorneio.criarEliminatoria(
                        "M17", FaseTorneio.OITAVAS, "Vencedor_M1", "Vencedor_M2");
        partida.definirParticipantes(mandante, visitante);
        return partida;
    }
}
