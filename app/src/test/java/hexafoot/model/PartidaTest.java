package hexafoot.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartidaTest {

    @Test
    void deveIniciarZeradaERegistrarGolsEEventos() {
        Time mandante = new Time("Mandante");
        Time visitante = new Time("Visitante");
        Partida partida = new Partida(mandante, visitante);
        EventoPartida evento = new EventoPartida(12, "GolMandante", jogador("Autor"));

        assertSame(mandante, partida.getMandante());
        assertSame(visitante, partida.getVisitante());
        assertEquals(0, partida.getGolsMandante());
        assertEquals(0, partida.getGolsVisitante());
        assertEquals(0, partida.getSubstituicoesMandante());
        assertEquals(0, partida.getSubstituicoesVisitante());
        assertTrue(partida.mandantePodeSubstituir());
        assertTrue(partida.visitantePodeSubstituir());
        assertTrue(partida.getEventos().isEmpty());

        partida.adicionarGolMandante();
        partida.adicionarGolVisitante();
        partida.adicionarEvento(evento);

        assertEquals(1, partida.getGolsMandante());
        assertEquals(1, partida.getGolsVisitante());
        assertEquals(1, partida.getEventos().size());
        assertSame(evento, partida.getEventos().get(0));
    }

    @Test
    void deveSubstituirMandanteEMoverJogadoresEntreListas() {
        Time mandante = new Time("Mandante");
        Time visitante = new Time("Visitante");
        Jogador sai = jogador("Sai");
        Jogador entra = jogador("Entra");
        Jogador permaneceReserva = jogador("Outra reserva");
        mandante.adicionarTitular(sai);
        mandante.adicionarReserva(entra);
        mandante.adicionarReserva(permaneceReserva);
        Partida partida = new Partida(mandante, visitante);

        boolean substituiu = partida.substituirMandante(sai, entra);

        assertTrue(substituiu);
        assertEquals(1, partida.getSubstituicoesMandante());
        assertFalse(mandante.getTitulares().contains(sai));
        assertTrue(mandante.getTitulares().contains(entra));
        assertTrue(mandante.getReservas().contains(sai));
        assertFalse(mandante.getReservas().contains(entra));
        assertFalse(partida.getReservasDisponiveisMandante().contains(sai));
        assertTrue(partida.getReservasDisponiveisMandante().contains(permaneceReserva));

        List<Jogador> copiaDisponiveis = partida.getReservasDisponiveisMandante();
        copiaDisponiveis.clear();
        assertTrue(mandante.getReservas().contains(permaneceReserva));
    }

    @Test
    void deveSubstituirVisitanteIndependentementeDoMandante() {
        Time mandante = new Time("Mandante");
        Time visitante = new Time("Visitante");
        Jogador sai = jogador("Sai visitante");
        Jogador entra = jogador("Entra visitante");
        visitante.adicionarTitular(sai);
        visitante.adicionarReserva(entra);
        Partida partida = new Partida(mandante, visitante);

        assertTrue(partida.substituirVisitante(sai, entra));

        assertEquals(0, partida.getSubstituicoesMandante());
        assertEquals(1, partida.getSubstituicoesVisitante());
        assertTrue(visitante.getTitulares().contains(entra));
        assertTrue(visitante.getReservas().contains(sai));
        assertFalse(partida.getReservasDisponiveisVisitante().contains(sai));
    }

    @Test
    void deveRejeitarSubstituicoesInvalidasSemAlterarElenco() {
        Time mandante = new Time("Mandante");
        Time visitante = new Time("Visitante");
        Jogador titular = jogador("Titular");
        Jogador reservaAtiva = jogador("Reserva ativa");
        Jogador reservaSuspensa = jogador("Reserva suspensa");
        reservaSuspensa.setStatus("Suspenso");
        mandante.adicionarTitular(titular);
        mandante.adicionarReserva(reservaAtiva);
        mandante.adicionarReserva(reservaSuspensa);
        Partida partida = new Partida(mandante, visitante);

        assertFalse(partida.substituirMandante(jogador("Fora do time"), reservaAtiva));
        assertFalse(partida.substituirMandante(titular, jogador("Não é reserva")));
        assertFalse(partida.substituirMandante(titular, reservaSuspensa));

        assertEquals(0, partida.getSubstituicoesMandante());
        assertEquals(List.of(titular), mandante.getTitulares());
        assertEquals(List.of(reservaAtiva, reservaSuspensa), mandante.getReservas());
    }

    @Test
    void deveBloquearSextaSubstituicao() {
        Time mandante = new Time("Mandante");
        Time visitante = new Time("Visitante");
        List<Jogador> titularesOriginais = new ArrayList<>();
        List<Jogador> reservasOriginais = new ArrayList<>();

        for (int i = 0; i < 11; i++) {
            Jogador titular = jogador("Titular " + i);
            titularesOriginais.add(titular);
            mandante.adicionarTitular(titular);
        }
        for (int i = 0; i < 6; i++) {
            Jogador reserva = jogador("Reserva " + i);
            reservasOriginais.add(reserva);
            mandante.adicionarReserva(reserva);
        }

        Partida partida = new Partida(mandante, visitante);
        for (int i = 0; i < 5; i++) {
            assertTrue(partida.substituirMandante(titularesOriginais.get(i), reservasOriginais.get(i)));
        }

        assertFalse(partida.mandantePodeSubstituir());
        assertFalse(partida.substituirMandante(titularesOriginais.get(5), reservasOriginais.get(5)));
        assertEquals(5, partida.getSubstituicoesMandante());
        assertTrue(mandante.getTitulares().contains(titularesOriginais.get(5)));
        assertTrue(mandante.getReservas().contains(reservasOriginais.get(5)));
    }

    @Test
    void deveAplicarVitoriaDoMandanteNaTabela() {
        Time mandante = new Time("Mandante");
        Time visitante = new Time("Visitante");
        Partida partida = new Partida(mandante, visitante);
        marcarGols(partida, 2, 1);

        partida.aplicarResultadoNaTabela();

        assertEquals(2, mandante.getGolsMarcados());
        assertEquals(1, mandante.getGolsSofridos());
        assertEquals(3, mandante.getPontos());
        assertEquals(1, mandante.getVitorias());
        assertEquals(1, visitante.getGolsMarcados());
        assertEquals(2, visitante.getGolsSofridos());
        assertEquals(0, visitante.getPontos());
        assertEquals(1, visitante.getDerrotas());
    }

    @Test
    void deveAplicarVitoriaDoVisitanteNaTabela() {
        Time mandante = new Time("Mandante");
        Time visitante = new Time("Visitante");
        Partida partida = new Partida(mandante, visitante);
        marcarGols(partida, 0, 2);

        partida.aplicarResultadoNaTabela();

        assertEquals(0, mandante.getPontos());
        assertEquals(1, mandante.getDerrotas());
        assertEquals(3, visitante.getPontos());
        assertEquals(1, visitante.getVitorias());
        assertEquals(2, visitante.getGolsMarcados());
        assertEquals(0, visitante.getGolsSofridos());
    }

    @Test
    void deveAplicarEmpateNaTabela() {
        Time mandante = new Time("Mandante");
        Time visitante = new Time("Visitante");
        Partida partida = new Partida(mandante, visitante);
        marcarGols(partida, 1, 1);

        partida.aplicarResultadoNaTabela();

        assertEquals(1, mandante.getPontos());
        assertEquals(1, visitante.getPontos());
        assertEquals(1, mandante.getEmpates());
        assertEquals(1, visitante.getEmpates());
        assertEquals(1, mandante.getGolsMarcados());
        assertEquals(1, visitante.getGolsMarcados());
    }

    private Jogador jogador(String nome) {
        return new Jogador(nome, "Atacante", 80, 40, 85, 20);
    }

    private void marcarGols(Partida partida, int golsMandante, int golsVisitante) {
        for (int i = 0; i < golsMandante; i++) {
            partida.adicionarGolMandante();
        }
        for (int i = 0; i < golsVisitante; i++) {
            partida.adicionarGolVisitante();
        }
    }
}
