package hexafoot.service.torneio;

import hexafoot.model.Partida;
import hexafoot.service.simulacao.RelogioPartida;

import java.io.Serializable;

/**
 * Executa partidas controladas pelo computador sem depender da interface gráfica.
 */
public class SimuladorPartidaCpu implements Serializable {
    private final RelogioPartida relogio;

    public SimuladorPartidaCpu() {
        this.relogio = new RelogioPartida();
        this.relogio.adicionarProcessadoresPadrao();
    }

    public void simularPartida(Partida partida) {
        relogio.simularJogoCompleto(partida);
    }
}