package hexafoot.service.simulacao;

import hexafoot.model.Partida;
import java.util.ArrayList;


/**
 * Entidade RelogioPartida - Motor central de tempo da simulação, 
 * responsável por avançar os minutos do jogo e notificar todos os processadores seguindo o padrão Observer.
 */

public class RelogioPartida {
    private ArrayList<ObserverMinuto> processadores;

    public RelogioPartida() {
        this.processadores = new ArrayList<>();
    }

    public void adicionarProcessador(ObserverMinuto processador) {
        this.processadores.add(processador);
    }

//-----------------Motor principal da partida-----------------
    private void avisarProcessadores(int minuto, Partida partida) {
            for (ObserverMinuto processador : processadores) {
                processador.atualizarMinuto(minuto, partida);
            }
    }

    public void simularJogo(Partida partida) {

        //primeiro tempo
        for (int i = 1; i <= 45; i ++) {
            avisarProcessadores(i, partida);
        }

        int acrescimos1T = (int) (Math.random() * 4); //número aleatório de acréscimos para o 1 tempo
        if (acrescimos1T > 0) {
            for (int i = 0; i < acrescimos1T; i ++) {
                avisarProcessadores(45, partida);
            }  
        }

        //segundo tempo
        for (int i = 46; i <= 90; i ++) {
            avisarProcessadores(i, partida);
        }

        int acrescimos2T = (int) (Math.random() * 5) + 2; //numero aleatório de acréscimos para o 2 tempo
        for (int i = 91; i <= 90 + acrescimos2T; i ++) {
            avisarProcessadores(i, partida);
        }
    }
}

