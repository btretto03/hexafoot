package hexafoot.service.simulacao;

import hexafoot.model.EventoPartida;
import hexafoot.model.Jogador;
import hexafoot.model.Partida;
import hexafoot.model.Time;

import java.io.Serializable;

/**
 * Aplica recuperação física e consequências disciplinares ou médicas após uma partida.
 */
public class GerenciadorPosJogo implements Serializable {

//-----------------Desgaste e recuperação física-----------------

    /**
     * Aplica aos titulares o desgaste equivalente a 90 minutos, usando o
     * multiplicador da tática atual do time.
     *
     * @param partida contexto da operação; não participa do cálculo atual
     */
    public void aplicarDesgasteFisico(Time time, Partida partida) { //reduz o físico dos jogadores que jogaram
        float multiplicador = (float) time.getTaticaAtual().getMultiplicadorDesgaste();

        for (Jogador jogador : time.getTitulares()) {
            jogador.consumirEnergia(90, multiplicador);
        }
    }

    /**
     * Recupera o elenco entre rodadas: titulares recebem 2 pontos de físico e
     * reservas recebem 10, respeitando o limite de 100.
     */
    public void regenerarFisicoElenco(Time time) { //recupera físico de todos entre rodadas (banco recupera mais)
        for (Jogador jogador : time.getTitulares()) {
            jogador.recuperarEnergiaPosJogo(true); //titular recupera pouco
        }

        for (Jogador jogador : time.getReservas()) {
            jogador.recuperarEnergiaPosJogo(false); //reserva recupera mais
        }
    }

//-----------------Controle disciplinar-----------------

    /**
     * Processa os eventos disciplinares de autores que ainda pertençam ao elenco:
     * dois amarelos acumulados ou um vermelho deixam o jogador suspenso.
     */
    public void processarCartoesAcumulados(Time time, Partida partida) { //varre eventos e aplica suspensão se acumulou 2 amarelos
        for (EventoPartida evento : partida.getEventos()) {
            Jogador autor = evento.getAutor();

            //só processa cartões de jogadores deste time
            if (time.getTitulares().contains(autor) == false && time.getReservas().contains(autor) == false) {
                continue;
            }

            if ("CartaoAmarelo".equals(evento.getTipo())) {
                if (autor.getCartoesAmarelos() >= 2) {
                    autor.setStatus("Suspenso");
                }
            }

            if ("CartaoVermelho".equals(evento.getTipo())) {
                autor.setStatus("Suspenso");
            }
        }
    }

    /**
     * Zera os cartões amarelos de titulares e reservas na transição para a semifinal.
     */
    public void limparCartoesFaseAvancada(Time time) { //zera os amarelos de todos quando chega na semifinal
        for (Jogador jogador : time.getTitulares()) {
            jogador.setCartoesAmarelos(0);
        }

        for (Jogador jogador : time.getReservas()) {
            jogador.setCartoesAmarelos(0);
        }
    }

//-----------------Departamento médico-----------------

    /**
     * Avança uma rodada de afastamento para todo o elenco. Como o estado de
     * afastamento é compartilhado pelo jogador, a operação também abrange suspensões;
     * ao chegar a zero, o status volta a {@code "Ativo"}.
     */
    public void atualizarStatusLesao(Time time) { //decrementa afastamento, quando chega em 0 volta a "Ativo"
        for (Jogador jogador : time.getTitulares()) {
            jogador.atualizarLesao();
        }

        for (Jogador jogador : time.getReservas()) {
            jogador.atualizarLesao();
        }
    }
}
