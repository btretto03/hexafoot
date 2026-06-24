package hexafoot.model.exception;

/**
 * Exceção lançada quando o usuário tenta escalar um jogador suspenso ou lesionado.
 */
public class JogadorIndisponivelException extends Exception {
    public JogadorIndisponivelException(String nomeJogador, String motivo) {
        super(nomeJogador + " não pode jogar: " + motivo);
    }
}
