package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Partida;

/**
 * Entidade ProcessadorFisico - Responsável por aplicar o desgaste de energia
 * em todos os jogadores titulares em campo a cada minuto da simulação.
 */

public class ProcessadorFisico implements ObserverMinuto {
    @Override
    public void atualizarMinuto(int minutoAtual, Partida partida) {

    }
}