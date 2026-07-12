package hexafoot.model;

import java.util.Objects;

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
    private final Time mandante;
    private final Time visitante;
    private final StatusPartidaTorneio status;


// -----Desing pattern factory para criar diferentes tipos de partida-----
    private PartidaTorneio(
            String id, FaseTorneio fase, Integer rodada, Grupo grupo, String identificadorOrigemMandante, String identificadorOrigemVisitante, Time mandante, Time visitante) {
        this.id = validarTexto(id, "O ID da partida");
        this.fase = Objects.requireNonNull(fase, "A fase da partida não pode ser nula");
        this.rodada = rodada;
        this.grupo = grupo;
        this.identificadorOrigemMandante = validarTexto(
                identificadorOrigemMandante, "O identificador de origem do mandante");
        this.identificadorOrigemVisitante = validarTexto(
                identificadorOrigemVisitante, "O identificador de origem do visitante");
        this.mandante = mandante;
        this.visitante = visitante;
        this.status = StatusPartidaTorneio.AGENDADA;
    }

    public static PartidaTorneio criarFaseDeGrupos(String id, int rodada, Grupo grupo, Time mandante, Time visitante) {
        Objects.requireNonNull(grupo, "O grupo da partida não pode ser nulo");
        Objects.requireNonNull(mandante, "O time mandante não pode ser nulo");
        Objects.requireNonNull(visitante, "O time visitante não pode ser nulo");

        if (rodada < 1 || rodada > 3) {
            throw new IllegalArgumentException("A rodada da fase de grupos deve estar entre 1 e 3");
        }
        if (mandante == visitante) {
            throw new IllegalArgumentException("Mandante e visitante devem ser times diferentes");
        }
        if (!grupo.contem(mandante) || !grupo.contem(visitante)) {
            throw new IllegalArgumentException("Os dois times devem pertencer ao grupo informado");
        }

        return new PartidaTorneio(id, FaseTorneio.FASE_DE_GRUPOS, rodada, grupo, mandante.getNome(), visitante.getNome(), mandante, visitante);
    }

    public static PartidaTorneio criarEliminatoria(String id,FaseTorneio fase,String identificadorOrigemMandante,String identificadorOrigemVisitante) {
        Objects.requireNonNull(fase, "A fase da partida não pode ser nula");

        if (fase == FaseTorneio.FASE_DE_GRUPOS || fase == FaseTorneio.ENCERRADO) {
            throw new IllegalArgumentException("A fase informada não representa uma partida eliminatória");
        }

        return new PartidaTorneio(id, fase, null, null, identificadorOrigemMandante, identificadorOrigemVisitante, null, null);
    }

    private static String validarTexto(String valor, String nomeCampo) {
        Objects.requireNonNull(valor, nomeCampo + " não pode ser nulo");
        String normalizado = valor.trim();

        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException(nomeCampo + " não pode ser vazio");
        }

        return normalizado;
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
}
