package hexafoot.service.simulacao;
/**
 * Classe para facilitar a mudança das constantes  usadas nas probabilidades da simulação
 * para deixar o jogo realista. Assim, quando estivermos testando o jogo rodando podemos ajustar
 * os valores aqui para aumentar ou diminuir a frequência de gols, cartões e lesões, por exemplo.
 */
public class RegrasSimulacao {

    // ---------------- GOLS ----------------
    public final  static int CHANCE_GOL = 40; 

    // ---------------- CARTÕES ----------------
    public final  static int CHANCE_BASE_FALTA = 2; 
    public final  static int CHANCE_BASE_VERMELHO = 1; 
    
    // ---------------- LESÕES ----------------
    public final  static int CHANCE_BASE_LESAO = 2; 
    public final  static int AFASTAMENTO_MIN_LESAO = 1;
    public final  static int AFASTAMENTO_MAX_LESAO = 3;

}