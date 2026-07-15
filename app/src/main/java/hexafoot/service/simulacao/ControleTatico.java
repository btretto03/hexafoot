package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Time;
import hexafoot.model.strategy.EstrategiaSimulacao;


/**
 * Organiza a escalação e as escolhas táticas de uma seleção antes da partida.
 */
public class ControleTatico {
    private Time time;
    private Jogador capitao;
    private Jogador batedorFalta;
    private Jogador batedorPenalti;

    public ControleTatico(Time time) {
        this.time = time;
        this.capitao = null;
        this.batedorFalta = null;
        this.batedorPenalti = null;
    }

//-----------------Escalação de titulares e reservas-----------------
    /**
     * Move um reserva para os titulares, respeitando o limite de 11 jogadores.
     *
     * @return {@code true} se o jogador estava na reserva e foi escalado
     */
    public boolean escalarJogadorTitular(Jogador jogador) { //move um jogador do banco para os titulares
        if (time.getTitulares().size() >= 11) {
            return false;
        }
        if (time.getReservas().contains(jogador) == false) {
            return false;
        }
        time.removerReserva(jogador);
        time.adicionarTitular(jogador);
        return true;
    }

    /**
     * Move um titular para a reserva.
     *
     * @return {@code true} se o jogador fazia parte dos titulares e foi movido
     */
    public boolean enviarParaOBanco(Jogador jogador) { //move um jogador dos titulares para o banco
        if (time.getTitulares().contains(jogador) == false) {
            return false;
        }
        time.removerTitular(jogador);
        time.adicionarReserva(jogador);
        return true;
    }

    /**
     * Verifica se a escalação contém exatamente 11 titulares com status {@code "Ativo"}.
     *
     * @return {@code true} somente quando as duas condições são atendidas
     */
    public boolean validarOnzeTitulares() { //exige exatamente 11 titulares ativos para validar a escalação
        if (time.getTitulares().size() != 11) {
            return false;
        }
        for (Jogador jogador : time.getTitulares()) {
            if ("Ativo".equals(jogador.getStatus()) == false) {
                return false;
            }
        }
        return true;
    }

    public void configurarPosturaEstrategica(EstrategiaSimulacao tatica) {
        time.setTaticaAtual(tatica);
    }

//-----------------Definicoes de cada jogador-----------------
    public void definirEspecialistas(Jogador capitao, Jogador batedorFalta, Jogador batedorPenalti) {
        this.capitao = capitao;
        this.batedorFalta = batedorFalta;
        this.batedorPenalti = batedorPenalti;
    }

//-----------------getters-----------------
    public Time getTime() {
        return time;
    }

    public Jogador getCapitao() {
        return capitao;
    }

    public Jogador getBatedorFalta() {
        return batedorFalta;
    }

    public Jogador getBatedorPenalti() {
        return batedorPenalti;
    }
}
