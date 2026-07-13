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