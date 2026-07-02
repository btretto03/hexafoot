package hexafoot.service.simulacao;

import hexafoot.model.EventoPartida;
import hexafoot.model.Jogador;
import hexafoot.model.Partida;
import hexafoot.model.Time;

/**
 * Entidade GerenciadorPosJogo, ela é responsável por aplicar as consequências após cada partida:
 * desgaste, recuperação, suspensões por cartões e evolução de lesões.
 */
public class GerenciadorPosJogo {

//-----------------Desgaste e recuperação física-----------------

    public void aplicarDesgasteFisico(Time time, Partida partida) { //reduz o físico dos jogadores que jogaram
        float multiplicador = (float) time.getTaticaAtual().getMultiplicadorDesgaste();

        for (Jogador jogador : time.getTitulares()) {
            jogador.consumirEnergia(90, multiplicador);
        }
    }

    public void regenerarFisicoElenco(Time time) { //recupera físico de todos entre rodadas (banco recupera mais)
        for (Jogador jogador : time.getTitulares()) {
            jogador.recuperarEnergiaPosJogo(true); //titular recupera pouco
        }

        for (Jogador jogador : time.getReservas()) {
            jogador.recuperarEnergiaPosJogo(false); //reserva recupera mais
        }
    }

//-----------------Controle disciplinar-----------------

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

    public void limparCartoesFaseAvancada(Time time) { //zera os amarelos de todos quando chega na semifinal
        for (Jogador jogador : time.getTitulares()) {
            jogador.setCartoesAmarelos(0);
        }

        for (Jogador jogador : time.getReservas()) {
            jogador.setCartoesAmarelos(0);
        }
    }

//-----------------Departamento médico-----------------

    public void atualizarStatusLesao(Time time) { //decrementa afastamento, quando chega em 0 volta a "Ativo"
        for (Jogador jogador : time.getTitulares()) {
            jogador.atualizarLesao();
        }

        for (Jogador jogador : time.getReservas()) {
            jogador.atualizarLesao();
        }
    }
}
