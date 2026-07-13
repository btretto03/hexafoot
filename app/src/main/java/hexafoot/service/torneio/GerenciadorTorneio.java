package hexafoot.service.torneio;

import hexafoot.dados.FabricaTorneio;
import hexafoot.model.FaseTorneio;
import hexafoot.model.Grupo;
import hexafoot.model.Partida;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.StatusPartidaTorneio;
import hexafoot.model.Time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Coordena o estado e as consultas gerais da Copa.
 */
public class GerenciadorTorneio {
    private static final int QUANTIDADE_SELECOES_INTERNACIONAIS = 47;

    //A classificação dos times é pelo critérios Mais Pontos -> Maior Saldo de Gols -> Mais Gols pró -> Ordem alfabética (Último caso)
    private static final Comparator<Time> COMPARADOR_CLASSIFICACAO = Comparator.comparingInt(Time::getPontos)
                            .reversed()
                            .thenComparing(Comparator.comparingInt(Time::getSaldoGols).reversed())
                            .thenComparing(Comparator.comparingInt(Time::getGolsMarcados).reversed())
                            .thenComparing(Time::getNome, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(Time::getNome);

    private final Time brasil;
    private final List<Grupo> grupos;
    private final List<PartidaTorneio> partidasFaseGrupos;
    private FaseTorneio faseAtual;
    private int rodadaAtual;

    public GerenciadorTorneio(Time brasil, List<Time> selecoesInternacionais) {
        this.brasil = brasil;

        if (selecoesInternacionais.size() != QUANTIDADE_SELECOES_INTERNACIONAIS) {
            throw new IllegalArgumentException("O torneio deve possuir exatamente 47 seleções internacionais");
        }

        List<Time> todasSelecoes = new ArrayList<>(selecoesInternacionais);
        todasSelecoes.add(brasil);

        FabricaTorneio fabricaTorneio = new FabricaTorneio();
        this.grupos = fabricaTorneio.montarGrupos(todasSelecoes);
        this.partidasFaseGrupos = fabricaTorneio.montarCalendarioFaseGrupos(grupos);
        this.faseAtual = FaseTorneio.FASE_DE_GRUPOS;
        this.rodadaAtual = 1;
    }

    public List<Time> getClassificacaoGrupo(String identificadorGrupo) {
        List<Time> classificacao = new ArrayList<>(buscarGrupo(identificadorGrupo).getTimes());
        classificacao.sort(COMPARADOR_CLASSIFICACAO);
        return List.copyOf(classificacao);
    }

    public List<PartidaTorneio> getPartidasDaRodada(int rodada) {
        if (rodada < 1 || rodada > 3) {
            throw new IllegalArgumentException("A rodada da fase de grupos deve estar entre 1 e 3");
        }

        return partidasFaseGrupos.stream().filter(partida -> Integer.valueOf(rodada).equals(partida.getRodada())).toList();
    }

    public Optional<PartidaTorneio> getProximaPartidaBrasil() {
        return partidasFaseGrupos.stream().filter(partida -> Integer.valueOf(rodadaAtual).equals(partida.getRodada())).filter(partida -> partida.getStatus() == StatusPartidaTorneio.AGENDADA).filter(this::envolveBrasil).findFirst();
    }

    public Partida iniciarPartida(String idPartida) {
        PartidaTorneio partidaTorneio = buscarPartida(idPartida);
        if (!Integer.valueOf(rodadaAtual).equals(partidaTorneio.getRodada())) {
            throw new IllegalStateException("Só é possível iniciar partidas da rodada atual");
        }

        return partidaTorneio.iniciar();
    }

    public boolean registrarResultado(String idPartida, Partida partida) {
        Objects.requireNonNull(partida, "A partida concluída não pode ser nula");
        PartidaTorneio partidaTorneio = buscarPartida(idPartida);

        if (partidaTorneio.getStatus() == StatusPartidaTorneio.CONCLUIDA) {
            return false;
        }
        if (partidaTorneio.getStatus() != StatusPartidaTorneio.EM_ANDAMENTO || partidaTorneio.getPartida() != partida) {
            throw new IllegalStateException("O resultado deve pertencer à partida iniciada pelo torneio");
        }

        partida.aplicarResultadoNaTabela();
        partidaTorneio.concluir();
        atualizarRodadaAtual();
        return true;
    }

    public boolean isFaseGruposConcluida() {
        return partidasFaseGrupos.stream().allMatch(partida -> partida.getStatus() == StatusPartidaTorneio.CONCLUIDA);
    }

    private Grupo buscarGrupo(String identificadorGrupo) {
        String identificadorNormalizado = identificadorGrupo.trim().toUpperCase();

        return grupos.stream().filter(grupo -> grupo.getIdentificador().equals(identificadorNormalizado)).findFirst().orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado: " + identificadorGrupo));
    }

    private PartidaTorneio buscarPartida(String idPartida) {
        Objects.requireNonNull(idPartida, "O ID da partida não pode ser nulo");
        return partidasFaseGrupos.stream().filter(partida -> partida.getId().equals(idPartida.trim())).findFirst().orElseThrow(() -> new IllegalArgumentException("Partida não encontrada: " + idPartida));
    }

    private void atualizarRodadaAtual() {
        boolean rodadaConcluida = getPartidasDaRodada(rodadaAtual).stream().allMatch(partida -> partida.getStatus() == StatusPartidaTorneio.CONCLUIDA);
        if (rodadaConcluida && rodadaAtual < 3) {
            rodadaAtual++;
        }
    }

    private boolean envolveBrasil(PartidaTorneio partida) {
        return partida.getMandante() == brasil || partida.getVisitante() == brasil;
    }

    public Time getBrasil() {
        return brasil;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public List<PartidaTorneio> getPartidasFaseGrupos() {
        return partidasFaseGrupos;
    }

    public FaseTorneio getFaseAtual() {
        return faseAtual;
    }

    public int getRodadaAtual() {
        return rodadaAtual;
    }
}
