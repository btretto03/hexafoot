package hexafoot.service.torneio;

import hexafoot.model.Partida;
import hexafoot.service.simulacao.RelogioPartida;

import java.util.Objects;

/**
 * Executa partidas controladas pelo computador sem depender da interface gráfica.
 */
public class SimuladorPartidaCpu {
    private final RelogioPartida relogio;

    public SimuladorPartidaCpu() {
        this.relogio = new RelogioPartida();
        this.relogio.adicionarProcessadoresPadrao();
    }

    public void simularPartida(Partida partida) {
        Objects.requireNonNull(partida, "A partida da CPU não pode ser nula");
        relogio.simularJogoCompleto(partida);
    }
}
