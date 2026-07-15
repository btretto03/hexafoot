package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Partida;

/**
 * Aplica, minuto a minuto, o desgaste físico definido pela posição, resistência
 * e tática de cada equipe.
 */

public class ProcessadorFisico implements ObserverMinuto {

    /**
     * Consome um minuto de energia de cada titular ativo das duas equipes.
     * Jogadores inativos não sofrem desgaste.
     */
    @Override
    public void atualizarMinuto(int minutoAtual, Partida partida) {

        float multiplicadorMandante = (float) partida.getMandante().getTaticaAtual().getMultiplicadorDesgaste();
        float multiplicadorVisitante = (float) partida.getVisitante().getTaticaAtual().getMultiplicadorDesgaste();

        for (Jogador jogador : partida.getMandante().getTitulares()) {
            if ("Ativo".equals(jogador.getStatus())) {
                jogador.consumirEnergia(1, multiplicadorMandante);
            }
        }

        for (Jogador jogador : partida.getVisitante().getTitulares()) {
            if ("Ativo".equals(jogador.getStatus())) {
                jogador.consumirEnergia(1, multiplicadorVisitante);
            }
        }
    }
}
