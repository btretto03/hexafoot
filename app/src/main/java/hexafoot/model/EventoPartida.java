package hexafoot.model;
/**
 * Entidade EventoPartida - Representa cada evento que ocorre durante a partida, como gols, cartões e substituições.
 */
public class EventoPartida {
    private int minuto;
    private String tipo;
    private Jogador autor;
    private Jogador jogadorSubstituinte;

   // Construtor para Gol, Cartão, Lesão
    public EventoPartida(int minuto, String tipo, Jogador autor) {
        this.minuto = minuto;
        this.tipo = tipo;
        this.autor = autor;
        this.jogadorSubstituinte = null;
    }

    // Construtor para Substituição
    public EventoPartida(int minuto, String tipo, Jogador autor, Jogador jogadorSubstituinte) {
        this.minuto = minuto;
        this.tipo = tipo;
        this.autor = autor; // quem sai
        this.jogadorSubstituinte = jogadorSubstituinte; // quem entra
    }

    //-----------------getters----------------- 
    /**Essa classe não tem setters
     *  por se tratar de eventos imutáveis */

    public int getMinuto() {
        return minuto;
    }
    public String getTipo() {
        return tipo;
    }
    public Jogador getAutor() {
        return autor;
    }
    public Jogador getJogadorSubstituinte() {
        return jogadorSubstituinte;
    }
}
