package hexafoot.service.simulacao;
/**
 * Classe para facilitar a mudança das constantes  usadas nas probabilidades da simulação
 * para deixar o jogo realista. Assim, quando estivermos testando o jogo rodando podemos ajustar
 * os valores aqui para aumentar ou diminuir a frequência de gols, cartões e lesões, por exemplo.
 */
public class RegrasSimulacao {

    // ---------------- GOLS ----------------
    public static int chanceBaseGol = 20; 

    // ---------------- CARTÕES ----------------
    public static int chanceBaseFalta = 4; 
    public static int chanceBaseVermelho = 8; 
    
    // ---------------- LESÕES ----------------
    public static int chanceBaseLesao = 8; 
    public static int afastamentoMinLesao = 1;
    public static int afastamentoMaxLesao = 3;

}