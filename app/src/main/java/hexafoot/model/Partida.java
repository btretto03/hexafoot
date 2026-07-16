package hexafoot.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Confronto mutável entre duas equipes, com placar, eventos e substituições.
 */
public class Partida implements Serializable {
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

    private List<Jogador> titularesOriginaisMandante;
    private List<Jogador> reservasOriginaisMandante;
    private List<Jogador> titularesOriginaisVisitante;
    private List<Jogador> reservasOriginaisVisitante;

    /**
     * Inicia um confronto em 0 a 0, com limite de cinco substituições por equipe.
     */
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

        this.titularesOriginaisMandante = new ArrayList<>(mandante.getTitulares());
        this.reservasOriginaisMandante = new ArrayList<>(mandante.getReservas());
        this.titularesOriginaisVisitante = new ArrayList<>(visitante.getTitulares());
        this.reservasOriginaisVisitante = new ArrayList<>(visitante.getReservas());
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

    /**
     * Lista reservas ativas do mandante que ainda não deixaram o campo nesta partida.
     * A lista retornada é independente da lista mantida pelo time.
     */
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

    /**
     * Lista reservas ativas do visitante que ainda não deixaram o campo nesta partida.
     * A lista retornada é independente da lista mantida pelo time.
     */
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

    /**
     * Troca um titular por uma reserva do mandante, movendo ambos entre as listas do
     * time e impedindo o retorno de quem saiu.
     *
     * @param sai titular que deixa o campo
     * @param entra reserva ativa que entra em campo
     * @return {@code false} sem alterações se o limite ou algum requisito não for atendido
     */
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

    /**
     * Troca um titular por uma reserva do visitante, movendo ambos entre as listas do
     * time e impedindo o retorno de quem saiu.
     *
     * @param sai titular que deixa o campo
     * @param entra reserva ativa que entra em campo
     * @return {@code false} sem alterações se o limite ou algum requisito não for atendido
     */
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
    /**
     * Acumula placar, vitória, empate ou derrota nas estatísticas dos dois times.
     * Deve ser chamado uma única vez para cada partida.
     */
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
    public void restaurarElencos() {
        restaurarTime(mandante, titularesOriginaisMandante, reservasOriginaisMandante);
        restaurarTime(visitante, titularesOriginaisVisitante, reservasOriginaisVisitante);
    }

    private void restaurarTime(Time time, List<Jogador> titularesOriginais, List<Jogador> reservasOriginais) {
        time.getTitulares().clear();
        time.getTitulares().addAll(titularesOriginais);
        time.getReservas().clear();
        time.getReservas().addAll(reservasOriginais);

        for (int i = 0; i < time.getTitulares().size(); i++) {
            Jogador j = time.getTitulares().get(i);
            if (!"Ativo".equals(j.getStatus())) {
                time.removerTitular(j);
                time.adicionarReserva(j);
                i--;

                Jogador substituto = null;
                for (Jogador r : time.getReservas()) {
                    if ("Ativo".equals(r.getStatus()) && r.getPosicao().equalsIgnoreCase(j.getPosicao())) {
                        substituto = r;
                        break;
                    }
                }
                if (substituto == null) {
                    for (Jogador r : time.getReservas()) {
                        if ("Ativo".equals(r.getStatus())) {
                            substituto = r;
                            break;
                        }
                    }
                }
                if (substituto != null) {
                    time.removerReserva(substituto);
                    time.adicionarTitular(substituto);
                }
            }
        }
    }
}
