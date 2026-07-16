package hexafoot.service.simulacao;
/**
 * Parâmetros ajustáveis da simulação. As probabilidades usam escalas diferentes,
 * indicadas em cada constante; os afastamentos são medidos em rodadas.
 */
public enum RegrasSimulacao {

    // ---------------- GOLS ----------------
    /** Limiar máximo por equipe e minuto, em uma rolagem de 1 a 1.000. */
    CHANCE_GOL(15),

    // ---------------- CARTÕES ----------------
    /** Limiar de cartão por jogador ativo e minuto, em uma rolagem de 1 a 1.000. */
    CHANCE_BASE_FALTA(2),
    /** Percentual base de vermelho direto após o sorteio de um cartão. */
    CHANCE_BASE_VERMELHO(1),
    
    // ---------------- LESÕES ----------------
    /** Limiar de lesão por jogador ativo e minuto, em uma rolagem de 1 a 10.000. */
    CHANCE_BASE_LESAO(1),
    /** Menor afastamento possível por lesão, em rodadas. */
    AFASTAMENTO_MIN_LESAO(1),
    /** Maior afastamento possível por lesão, em rodadas. */
    AFASTAMENTO_MAX_LESAO(3);

    private final int valor;

    RegrasSimulacao(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }
}
