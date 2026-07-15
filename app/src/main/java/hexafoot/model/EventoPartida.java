package hexafoot.model;

import java.io.Serializable;

/**
 * Ocorrência registrada no histórico de uma partida, como gol, cartão, lesão ou
 * substituição.
 */
public class EventoPartida implements Serializable {
    private int minuto;
    private String tipo;
    private Jogador autor;
    private Jogador jogadorSubstituinte;

    /**
     * Cria um evento associado a um único jogador.
     *
     * @param tipo identificador textual usado pelos processadores e pela formatação
     *             da interface
     */
    public EventoPartida(int minuto, String tipo, Jogador autor) {
        this.minuto = minuto;
        this.tipo = tipo;
        this.autor = autor;
        this.jogadorSubstituinte = null;
    }

    /**
     * Cria um evento com dois jogadores, usado para substituições.
     *
     * @param autor jogador que deixa o campo
     * @param jogadorSubstituinte jogador que entra em campo
     */
    public EventoPartida(int minuto, String tipo, Jogador autor, Jogador jogadorSubstituinte) {
        this.minuto = minuto;
        this.tipo = tipo;
        this.autor = autor; // quem sai
        this.jogadorSubstituinte = jogadorSubstituinte; // quem entra
    }

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

    //-----------------Formatação para a Interface Gráfica-----------------
    /**
     * Formata tipos conhecidos por palavras-chave, sem distinção entre maiúsculas e
     * minúsculas; tipos desconhecidos usam a representação genérica.
     */
    @Override
    public String toString() {
        // Garantimos que o nome do jogador seja pego corretamente
        String nomeAutor = (autor != null) ? autor.getNome() : "Desconhecido";
        
        // Padronizamos o texto do tipo para evitar problemas com letras maiúsculas/minúsculas
        String tipoFormatado = (tipo != null) ? tipo.toLowerCase() : "";

        // Retornamos uma mensagem estilizada dependendo do tipo de evento
        if (tipoFormatado.contains("gol")) {
            return "⚽ GOL! " + nomeAutor + " manda para o fundo das redes!";
        } 
        else if (tipoFormatado.contains("amarelo")) {
            return "🟨 Cartão Amarelo para " + nomeAutor + ".";
        } 
        else if (tipoFormatado.contains("vermelho")) {
            return "🟥 EXPULSO! Cartão Vermelho direto para " + nomeAutor + "!";
        } 
        else if (tipoFormatado.contains("lesão") || tipoFormatado.contains("lesao")) {
            return "🚑 Sentiu! " + nomeAutor + " sofreu uma lesão e precisa de atendimento.";
        } 
        else if (tipoFormatado.contains("substituição") || tipoFormatado.contains("substituicao")) {
            String nomeEntra = (jogadorSubstituinte != null) ? jogadorSubstituinte.getNome() : "Reserva";
            return "🔄 Substituição: Sai " + nomeAutor + ", entra " + nomeEntra + ".";
        } 
        else {
            return tipo + " - " + nomeAutor;
        }
    }
}
