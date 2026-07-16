package hexafoot.service.simulacao;

import hexafoot.model.Partida;
import hexafoot.model.Time;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Notifica, em ordem de registro, os processadores de cada minuto da partida.
 * Permite tanto a execução passo a passo quanto a simulação completa.
 */
public class RelogioPartida implements Serializable {
    private ArrayList<ObserverMinuto> processadores;

    public RelogioPartida() {
        this.processadores = new ArrayList<>();
    }

    /**
     * Acrescenta um processador ao fim da sequência de notificações.
     */
    public void adicionarProcessador(ObserverMinuto processador) {
        this.processadores.add(processador);
    }

    //-----------------Registro dos processadores-----------------
    /**
     * Acrescenta os processadores padrão, com substituição automática por lesão
     * para as duas equipes.
     */
    public void adicionarProcessadoresPadrao() {
        adicionarProcessadoresPadrao(null);
    }

    /**
     * Acrescenta, sem remover registros anteriores, os processadores de físico,
     * lesões, cartões e gols, nessa ordem.
     *
     * @param timeSemAutoSubstituicao equipe controlada pelo usuário, identificada
     *                               pela mesma instância; {@code null} automatiza ambas
     */
    public void adicionarProcessadoresPadrao(Time timeSemAutoSubstituicao) {
        this.adicionarProcessador(new ProcessadorFisico());
        this.adicionarProcessador(new ProcessadorLesoes(timeSemAutoSubstituicao));
        this.adicionarProcessador(new ProcessadorCartoes());
        this.adicionarProcessador(new ProcessadorGols());
    }

    //-----------------Motor principal da partida-----------------
    /**
     * Notifica cada processador na ordem em que foi adicionado.
     */
    private void avisarProcessadores(int minuto, Partida partida) {
        for (ObserverMinuto processador : processadores) {
            processador.atualizarMinuto(minuto, partida);
        }
    }

    /**
     * Executa uma única notificação sem manter ou validar a progressão do relógio.
     * Chamadas repetidas para o mesmo minuto reaplicam todos os processamentos.
     */
    public void processarMinutoIsolado(int minuto, Partida partida) {
        avisarProcessadores(minuto, partida);
    }

    /**
     * Simula 90 minutos, de 0 a 3 passos de acréscimo no primeiro tempo e de
     * 2 a 6 no segundo. Os acréscimos do primeiro tempo são registrados novamente
     * como minuto 45.
     */
    public void simularJogoCompleto(Partida partida) {

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
