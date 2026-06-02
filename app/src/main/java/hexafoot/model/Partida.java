package hexafoot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Partida - Representa o confronto entre duas equipes, armazenando o placar e o histórico de eventos que ocorrem durante o jogo.
 */
public class Partida {
    private Time mandante;
    private Time visitante;
    private int golsMandante;
    private int golsVisitante;
    private List<EventoPartida> eventos;

    public Partida(Time mandante, Time visitante) {
        this.mandante = mandante;
        this.visitante = visitante;
        this.golsMandante = 0;
        this.golsVisitante = 0;
        this.eventos = new ArrayList<>();
    }

    //-----------------Métodos de ação do jogo-----------------
    public void adicionarGolMandante() {
        this.golsMandante++;
    }

    public void adicionarGolVisitante() {
        this.golsVisitante++;
    }

    public void adicionarEvento(EventoPartida evento) {
        this.eventos.add(evento);
    }

    //-----------------getters-----------------
    public Time getMandante() {
        return mandante;
    }

    public Time getVisitante() {
        return visitante;
    }

    public int getGolsMandante() {
        return golsMandante;
    }

    public int getGolsVisitante() {
        return golsVisitante;
    }

    public List<EventoPartida> getEventos() {
        return eventos;
    }
}
