package hexafoot.service.simulacao;
import hexafoot.model.Partida;

/**
 * Interface ObserverMinuto - Implementamos o  padrão Observer para o relógio do jogo.
 * Qualquer evento (gols, faltas, lesões) que implementar esta interface 
 * será notificada a cada minuto para printar durante o andamento do jogo.
 */
public interface ObserverMinuto {
    void atualizarMinuto(int minutoAtual, Partida partida);
}
