package hexafoot.service.simulacao;
/**
 * Classe para facilitar a mudança das constantes  usadas nas probabilidades da simulação
 * para deixar o jogo realista. Assim, quando estivermos testando o jogo rodando podemos ajustar
 * os valores aqui para aumentar ou diminuir a frequência de gols, cartões e lesões, por exemplo.
 */
public enum RegrasSimulacao {

    // ---------------- GOLS ----------------
    CHANCE_GOL(15),

    // ---------------- CARTÕES ----------------
    CHANCE_BASE_FALTA(2),
    CHANCE_BASE_VERMELHO(1),
    
    // ---------------- LESÕES ----------------
    CHANCE_BASE_LESAO(1),
    AFASTAMENTO_MIN_LESAO(1),
    AFASTAMENTO_MAX_LESAO(3);

    private final int valor;

    RegrasSimulacao(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }
}