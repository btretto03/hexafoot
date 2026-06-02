package hexafoot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Partida - Representa o confronto entre duas equipes, armazenando o placar e o histórico de eventos que ocorrem durante o jogo.
 */
public class Partida {
    private int maxSubstituicoes;

    private Time mandante;
    private Time visitante;
    private int golsMandante;
    private int golsVisitante;
    private List<EventoPartida> eventos;

    private int substituicoesMandante;
    private int substituicoesVisitante;

    private List<Jogador> mandanteJaSaidos; //preciso guardar os nomes porque eles nao podem voltar ao jogo
    private List<Jogador> visitanteJaSaidos;

    public Partida(Time mandante, Time visitante) {
        this.mandante = mandante;
        this.visitante = visitante;
        this.golsMandante = 0;
        this.golsVisitante = 0;
        this.eventos = new ArrayList<>();

        this.maxSubstituicoes = 5;

        this.substituicoesMandante = 0;
        this.substituicoesVisitante = 0;
        this.mandanteJaSaidos = new ArrayList<>();
        this.visitanteJaSaidos = new ArrayList<>();
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

    //-----------------Regras de substituicao-----------------
    public int getSubstituicoesMandante() {
        return substituicoesMandante;
    }

    public int getSubstituicoesVisitante() {
        return substituicoesVisitante;
    }

    public boolean mandantePodeSubstituir() {
        return substituicoesMandante < maxSubstituicoes;
    }

    public boolean visitantePodeSubstituir() {
        return substituicoesVisitante < maxSubstituicoes;
    }

    public List<Jogador> getReservasDisponiveisMandante() {
        List<Jogador> disponiveis = new ArrayList<>();
        for (Jogador reserva : mandante.getReservas()) {
            if ("Ativo".equals(reserva.getStatus())) {
                if (mandanteJaSaidos.contains(reserva) == false) {
                    disponiveis.add(reserva);
                }
            }
        }
        return disponiveis;
    }

    public List<Jogador> getReservasDisponiveisVisitante() {
        List<Jogador> disponiveis = new ArrayList<>();
        for (Jogador reserva : visitante.getReservas()) {
            if ("Ativo".equals(reserva.getStatus())) {
                if (visitanteJaSaidos.contains(reserva) == false) {
                    disponiveis.add(reserva);
                }
            }
        }
        return disponiveis;
    }

    public boolean substituirMandante(Jogador sai, Jogador entra) {
        boolean pode = mandantePodeSubstituir();
        if (pode == false) {
            return false;
        }
        boolean titular = mandante.getTitulares().contains(sai);
        if (titular == false) {
            return false;
        }
        boolean reserva = mandante.getReservas().contains(entra);
        if (reserva == false) {
            return false;
        }
        boolean ativo = "Ativo".equals(entra.getStatus());
        if (ativo == false) {
            return false;
        }

        mandante.removerTitular(sai);
        
        // quem sai volta para o banco e não pode retornar nesta partida
        boolean contemSai = mandante.getReservas().contains(sai);
        if (contemSai == false) {
            mandante.adicionarReserva(sai);
        }

        mandante.removerReserva(entra);
        mandante.adicionarTitular(entra);

        mandanteJaSaidos.add(sai);
        substituicoesMandante ++;
        return true;
    }

    public boolean substituirVisitante(Jogador sai, Jogador entra) {
        boolean pode = visitantePodeSubstituir();
        if (pode == false) {
            return false;
        }
        boolean titular = visitante.getTitulares().contains(sai);
        if (titular == false) {
            return false;
        }
        boolean reserva = visitante.getReservas().contains(entra);
        if (reserva == false) {
            return false;
        }
        boolean ativo = "Ativo".equals(entra.getStatus());
        if (ativo == false) {
            return false;
        }

        visitante.removerTitular(sai);
        boolean contemSai = visitante.getReservas().contains(sai);
        if (contemSai == false) {
            visitante.adicionarReserva(sai);
        }

        visitante.removerReserva(entra);
        visitante.adicionarTitular(entra);
        visitanteJaSaidos.add(sai);
        substituicoesVisitante++;
        return true;
    }

    //-----------------Aplicação do resultado na tabela-----------------
    public void aplicarResultadoNaTabela() {
        mandante.setGolsMarcados(mandante.getGolsMarcados() + golsMandante);
        mandante.setGolsSofridos(mandante.getGolsSofridos() + golsVisitante);
        visitante.setGolsMarcados(visitante.getGolsMarcados() + golsVisitante);
        visitante.setGolsSofridos(visitante.getGolsSofridos() + golsMandante);

        if (golsMandante > golsVisitante) {
            mandante.registrarVitoria();
            visitante.registrarDerrota();
        } else if (golsVisitante > golsMandante) {
            visitante.registrarVitoria();
            mandante.registrarDerrota();
        } else {
            mandante.registrarEmpate();
            visitante.registrarEmpate();
        }
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
