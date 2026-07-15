package hexafoot.service.simulacao;

import hexafoot.model.EventoPartida;
import hexafoot.model.Jogador;
import hexafoot.model.Partida;
import hexafoot.model.Time;
import hexafoot.model.strategy.TaticaOfensiva;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GerenciadorPosJogoTest {

    private final GerenciadorPosJogo gerenciador = new GerenciadorPosJogo();

    @Test
    void deveAplicarDesgasteFisicoDaTaticaAtual() {
        Time time = new Time("Brasil");
        Jogador atacante = ApoioTestes.jogador("Atacante", "Atacante", 80, 40, 85, 10);
        time.adicionarTitular(atacante);
        time.setTaticaAtual(new TaticaOfensiva());
        Partida partida = new Partida(time, new Time("Adversário"));

        gerenciador.aplicarDesgasteFisico(time, partida);

        assertEquals(89, atacante.getFisico(),
                "A tática ofensiva deveria aumentar o desgaste para onze pontos");
    }

    @Test
    void deveRecuperarEnergiaConformeParticipacao() {
        Time time = new Time("Brasil");
        Jogador titular = ApoioTestes.jogador("Titular", "Atacante");
        Jogador reserva = ApoioTestes.jogador("Reserva", "Atacante");
        titular.setFisico(50);
        reserva.setFisico(50);
        time.adicionarTitular(titular);
        time.adicionarReserva(reserva);

        gerenciador.regenerarFisicoElenco(time);

        assertEquals(52, titular.getFisico(), "O titular deveria recuperar dois pontos");
        assertEquals(60, reserva.getFisico(), "O reserva deveria recuperar dez pontos");
    }

    @Test
    void deveAplicarSuspensoesDosEventosDoTime() {
        Time time = new Time("Brasil");
        Time adversario = new Time("Adversário");
        Jogador porAmarelos = ApoioTestes.jogador("Dois amarelos", "Defensor");
        Jogador porVermelho = ApoioTestes.jogador("Vermelho", "Defensor");
        Jogador foraDoElenco = ApoioTestes.jogador("Outro time", "Defensor");
        time.adicionarTitular(porAmarelos);
        time.adicionarTitular(porVermelho);
        porAmarelos.aplicarCartaoAmarelo();
        porAmarelos.aplicarCartaoAmarelo();
        porAmarelos.setStatus("Ativo");
        Partida partida = new Partida(time, adversario);
        partida.adicionarEvento(new EventoPartida(20, "CartaoAmarelo", porAmarelos));
        partida.adicionarEvento(new EventoPartida(30, "CartaoVermelho", porVermelho));
        partida.adicionarEvento(new EventoPartida(40, "CartaoVermelho", foraDoElenco));

        gerenciador.processarCartoesAcumulados(time, partida);

        assertEquals("Suspenso", porAmarelos.getStatus(), "Dois amarelos deveriam suspender o jogador");
        assertEquals("Suspenso", porVermelho.getStatus(), "O vermelho deveria suspender o jogador");
        assertEquals("Ativo", foraDoElenco.getStatus(), "Um jogador externo não deveria ser alterado");
    }

    @Test
    void deveAtualizarAfastamentoELimparCartoes() {
        Time time = new Time("Brasil");
        Jogador titular = ApoioTestes.jogador("Titular", "Atacante");
        Jogador reserva = ApoioTestes.jogador("Reserva", "Defensor");
        time.adicionarTitular(titular);
        time.adicionarReserva(reserva);
        titular.sofrerLesao(2);
        titular.aplicarCartaoAmarelo();
        reserva.aplicarCartaoAmarelo();

        gerenciador.atualizarStatusLesao(time);
        gerenciador.limparCartoesFaseAvancada(time);

        assertEquals(1, titular.getRodadasAfastamento(), "Deveria restar uma rodada de lesão");
        assertEquals("Lesionado", titular.getStatus(), "O jogador ainda deveria estar lesionado");
        assertEquals(0, titular.getCartoesAmarelos(), "Os amarelos do titular deveriam ser zerados");
        assertEquals(0, reserva.getCartoesAmarelos(), "Os amarelos do reserva deveriam ser zerados");

        gerenciador.atualizarStatusLesao(time);

        assertEquals(0, titular.getRodadasAfastamento(), "O afastamento deveria terminar");
        assertEquals("Ativo", titular.getStatus(), "O jogador deveria voltar a ficar ativo");
    }
}
