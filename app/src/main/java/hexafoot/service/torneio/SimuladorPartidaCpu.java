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

    /**
     * Percorre o relógio completo e altera placar, eventos e jogadores por meio dos
     * processadores padrão. Não registra o resultado no torneio nem disputa pênaltis.
     */
    public void simularPartida(Partida partida) {
        relogio.simularJogoCompleto(partida);
    }
}
