package hexafoot.model;

/**
 * Metadados de uma partida agendada no calendário da Copa.
 */
public class PartidaTorneio {
    private final String id;
    private final FaseTorneio fase;
    private final Integer rodada;
    private final Grupo grupo;
    private final String identificadorOrigemMandante;
    private final String identificadorOrigemVisitante;
    private Time mandante;
    private Time visitante;
    private StatusPartidaTorneio status;
    private Partida partida;


// -----Desing pattern factory para criar diferentes tipos de partida-----
    private PartidaTorneio(String id, FaseTorneio fase, Integer rodada, Grupo grupo, String identificadorOrigemMandante, String identificadorOrigemVisitante, Time mandante, Time visitante) {
        this.id = id.trim();
        this.fase = fase;
        this.rodada = rodada;
        this.grupo = grupo;
        this.identificadorOrigemMandante = identificadorOrigemMandante;
        this.identificadorOrigemVisitante = identificadorOrigemVisitante;
        this.mandante = mandante;
        this.visitante = visitante;
        this.status = StatusPartidaTorneio.AGENDADA;
        this.partida = null;
    }

    public static PartidaTorneio criarFaseDeGrupos(String id, int rodada, Grupo grupo, Time mandante, Time visitante) {
        return new PartidaTorneio(id, FaseTorneio.FASE_DE_GRUPOS, rodada, grupo, mandante.getNome(), visitante.getNome(), mandante, visitante);
    }

    public static PartidaTorneio criarEliminatoria(String id, FaseTorneio fase, String identificadorOrigemMandante, String identificadorOrigemVisitante) {
        return new PartidaTorneio(id, fase, null, null, identificadorOrigemMandante, identificadorOrigemVisitante, null, null);
    }

    public Partida iniciar() {
        this.partida = new Partida(mandante, visitante);
        this.status = StatusPartidaTorneio.EM_ANDAMENTO;
        return partida;
    }

    public void concluir() {
        this.status = StatusPartidaTorneio.CONCLUIDA;
    }

    public void definirParticipantes(Time mandante, Time visitante) {
        this.mandante = mandante;
        this.visitante = visitante;
    }

    public String getId() {
        return id;
    }

    public FaseTorneio getFase() {
        return fase;
    }

    public Integer getRodada() {
        return rodada;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public String getIdentificadorOrigemMandante() {
        return identificadorOrigemMandante;
    }

    public String getIdentificadorOrigemVisitante() {
        return identificadorOrigemVisitante;
    }

    public Time getMandante() {
        return mandante;
    }

    public Time getVisitante() {
        return visitante;
    }

    public StatusPartidaTorneio getStatus() {
        return status;
    }

    public Partida getPartida() {
        return partida;
    }
}
