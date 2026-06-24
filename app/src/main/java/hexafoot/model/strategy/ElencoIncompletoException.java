package hexafoot.model.strategy;

/**
 * Exceção lançada quando a convocação não tem exatamente 26 jogadores.
 */
public class ElencoIncompletoException extends Exception {
    public ElencoIncompletoException(int totalAtual) {
        super("Elenco incompleto: " + totalAtual + " convocados de 26 necessários.");
    }
}
