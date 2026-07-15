package hexafoot.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class EventoPartidaTest {

    @Test
    void deveCriarEventoComUmJogador() {
        Jogador autor = jogador("Autor");

        EventoPartida evento = new EventoPartida(37, "GolMandante", autor);

        assertEquals(37, evento.getMinuto());
        assertEquals("GolMandante", evento.getTipo());
        assertSame(autor, evento.getAutor());
        assertNull(evento.getJogadorSubstituinte());
    }

    @Test
    void deveCriarEventoDeSubstituicaoComJogadorDeEntrada() {
        Jogador sai = jogador("Sai");
        Jogador entra = jogador("Entra");

        EventoPartida evento = new EventoPartida(60, "Substituicao", sai, entra);

        assertSame(sai, evento.getAutor());
        assertSame(entra, evento.getJogadorSubstituinte());
    }

    @Test
    void deveFormatarGolSemDistinguirMaiusculasEMinusculas() {
        EventoPartida evento = new EventoPartida(10, "gOlVisitante", jogador("Marta"));

        assertEquals("⚽ GOL! Marta manda para o fundo das redes!", evento.toString());
    }

    @Test
    void deveFormatarCartoesELesao() {
        Jogador autor = jogador("Marta");

        EventoPartida amarelo = new EventoPartida(20, "SegundoAmarelo", autor);
        EventoPartida vermelho = new EventoPartida(30, "CartaoVermelho", autor);
        EventoPartida lesao = new EventoPartida(40, "Lesao", autor);

        assertEquals("🟨 Cartão Amarelo para Marta.", amarelo.toString());
        assertEquals("🟥 EXPULSO! Cartão Vermelho direto para Marta!", vermelho.toString());
        assertEquals("🚑 Sentiu! Marta sofreu uma lesão e precisa de atendimento.", lesao.toString());
    }

    @Test
    void deveFormatarSubstituicaoComOuSemReservaDefinida() {
        Jogador sai = jogador("Marta");
        Jogador entra = jogador("Formiga");

        EventoPartida completa = new EventoPartida(55, "Substituição", sai, entra);
        EventoPartida semReserva = new EventoPartida(56, "Substituicao", sai);

        assertEquals("🔄 Substituição: Sai Marta, entra Formiga.", completa.toString());
        assertEquals("🔄 Substituição: Sai Marta, entra Reserva.", semReserva.toString());
    }

    @Test
    void deveUsarFormatoGenericoETratarAutorAusente() {
        EventoPartida generico = new EventoPartida(120, "PenaltiPerdidoMandante", jogador("Marta"));
        EventoPartida nulo = new EventoPartida(0, null, null);

        assertEquals("PenaltiPerdidoMandante - Marta", generico.toString());
        assertEquals("null - Desconhecido", nulo.toString());
    }

    private Jogador jogador(String nome) {
        return new Jogador(nome, "Atacante", 80, 40, 85, 20);
    }
}
