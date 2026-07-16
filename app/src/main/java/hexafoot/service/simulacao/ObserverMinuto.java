package hexafoot.service.simulacao;
import hexafoot.model.Partida;

import java.io.Serializable;

/**
 * Processador notificado pelo relógio a cada passo da simulação.
 * As implementações podem alterar a partida e os jogadores das duas equipes.
 */
public interface ObserverMinuto extends Serializable {
    /**
     * Processa um passo de tempo. Um mesmo número de minuto pode ser notificado
     * mais de uma vez durante os acréscimos do primeiro tempo.
     *
     * @param minutoAtual minuto exibido para os eventos gerados neste passo
     * @param partida estado mutável da partida
     */
    void atualizarMinuto(int minutoAtual, Partida partida);
}
