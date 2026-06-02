package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Partida;

/**
 * Entidade ProcessadorFisico - Especialista responsável por aplicar o desgaste de energia
 * em todos os jogadores titulares em campo a cada minuto da simulação.
 */

public class ProcessadorFisico implements ObserverMinuto {
    @Override
    public void atualizarMinuto(int minutoAtual, Partida partida) {
        
        for (Jogador jogador : partida.getMandante().getTitulares()) {
            jogador.consumirEnergia(1, 1.0f);
        }
        
        for (Jogador jogador : partida.getVisitante().getTitulares()) {
            jogador.consumirEnergia(1, 1.0f);
        }
    }
}