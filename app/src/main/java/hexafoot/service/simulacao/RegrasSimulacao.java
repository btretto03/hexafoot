package hexafoot.service.simulacao;
/**
 * Classe para facilitar a mudança das constantes  usadas nas probabilidades da simulação
 * para deixar o jogo realista. Assim, quando estivermos testando o jogo rodando podemos ajustar
 * os valores aqui para aumentar ou diminuir a frequência de gols, cartões e lesões, por exemplo.
 */
public class RegrasSimulacao {

    // ---------------- GOLS ----------------
    public static int chanceBaseGol = 40; 

    // ---------------- CARTÕES ----------------
    public static int chanceBaseFalta = 2; 
    public static int chanceBaseVermelho = 1; 
    
    // ---------------- LESÕES ----------------
    public static int chanceBaseLesao = 2; 
    public static int afastamentoMinLesao = 1;
    public static int afastamentoMaxLesao = 3;

}