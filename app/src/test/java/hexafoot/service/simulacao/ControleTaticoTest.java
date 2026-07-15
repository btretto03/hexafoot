package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Time;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControleTaticoTest {

    @Test
    void deveMoverReservaParaOsTitulares() {
        Time time = new Time("Brasil");
        Jogador reserva = ApoioTestes.jogador("Reserva", "Atacante");
        time.adicionarReserva(reserva);
        ControleTatico controle = new ControleTatico(time);

        boolean escalado = controle.escalarJogadorTitular(reserva);

        assertTrue(escalado, "A escalação deveria ser aceita");
        assertEquals(1, time.getTitulares().size(), "Deveria haver um titular");
        assertSame(reserva, time.getTitulares().get(0), "O reserva deveria virar titular");
        assertFalse(time.getReservas().contains(reserva), "O jogador deveria sair da reserva");
    }

    @Test
    void deveRecusarEscalacaoAcimaDoLimite() {
        Time time = ApoioTestes.timeComTitulares("Brasil", 11);
        Jogador reserva = ApoioTestes.jogador("Décimo segundo", "Atacante");
        time.adicionarReserva(reserva);
        ControleTatico controle = new ControleTatico(time);

        boolean escalado = controle.escalarJogadorTitular(reserva);

        assertFalse(escalado, "A escalação acima do limite deveria ser recusada");
        assertEquals(11, time.getTitulares().size(), "O total de titulares não deveria mudar");
        assertTrue(time.getReservas().contains(reserva), "O jogador deveria permanecer na reserva");
    }

    @Test
    void deveMoverTitularParaOBanco() {
        Time time = ApoioTestes.timeComTitulares("Brasil", 1);
        Jogador titular = time.getTitulares().get(0);
        ControleTatico controle = new ControleTatico(time);

        boolean primeiraTentativa = controle.enviarParaOBanco(titular);
        boolean segundaTentativa = controle.enviarParaOBanco(titular);

        assertTrue(primeiraTentativa, "A primeira movimentação deveria ocorrer");
        assertFalse(segundaTentativa, "O jogador não deveria ser movido novamente");
        assertFalse(time.getTitulares().contains(titular), "O jogador deveria sair dos titulares");
        assertTrue(time.getReservas().contains(titular), "O jogador deveria entrar na reserva");
    }

    @Test
    void deveExigirOnzeTitularesAtivos() {
        Time time = ApoioTestes.timeComTitulares("Brasil", 11);
        ControleTatico controle = new ControleTatico(time);

        assertTrue(controle.validarOnzeTitulares(), "Onze jogadores ativos deveriam ser válidos");

        time.getTitulares().get(0).aplicarCartaoVermelho();

        assertFalse(controle.validarOnzeTitulares(), "Um titular suspenso deveria invalidar a escalação");
    }
}
