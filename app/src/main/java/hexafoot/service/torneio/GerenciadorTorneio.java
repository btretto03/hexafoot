package hexafoot.service.torneio;

import hexafoot.dados.FabricaTorneio;
import hexafoot.model.FaseTorneio;
import hexafoot.model.Grupo;
import hexafoot.model.Partida;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.StatusPartidaTorneio;
import hexafoot.model.Time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Coordena o estado e as consultas gerais da Copa.
 */
public class GerenciadorTorneio {

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
    private final List<PartidaTorneio> partidasMataMata;
    private final SimuladorPartidaCpu simuladorPartidaCpu;
    private FaseTorneio faseAtual;
    private int rodadaAtual;

    public GerenciadorTorneio(Time brasil, List<Time> selecoesInternacionais) {
        this.brasil = brasil;
        List<Time> todasSelecoes = new ArrayList<>(selecoesInternacionais);
        todasSelecoes.add(brasil);
        FabricaTorneio fabricaTorneio = new FabricaTorneio();
        this.grupos = fabricaTorneio.montarGrupos(todasSelecoes);
        this.partidasFaseGrupos = fabricaTorneio.montarCalendarioFaseGrupos(grupos);
        this.partidasMataMata = fabricaTorneio.montarChaveamentoMataMata();
        this.simuladorPartidaCpu = new SimuladorPartidaCpu();
        this.faseAtual = FaseTorneio.FASE_DE_GRUPOS;
        this.rodadaAtual = 1;
    }

    public List<Time> getClassificacaoGrupo(String identificadorGrupo) {
        List<Time> classificacao = new ArrayList<>(buscarGrupo(identificadorGrupo).getTimes());
        classificacao.sort(COMPARADOR_CLASSIFICACAO);
        return List.copyOf(classificacao);
    }

    public List<PartidaTorneio> getPartidasDaRodada(int rodada) {
        return partidasFaseGrupos.stream().filter(partida -> Integer.valueOf(rodada).equals(partida.getRodada())).toList();
    }

    public Optional<PartidaTorneio> getProximaPartidaBrasil() {
        return partidasFaseGrupos.stream().filter(partida -> Integer.valueOf(rodadaAtual).equals(partida.getRodada()))
                                .filter(partida -> partida.getStatus() == StatusPartidaTorneio.AGENDADA)
                                .filter(this::envolveBrasil).findFirst();
    }

    public Partida iniciarPartida(String idPartida) {
        PartidaTorneio partidaTorneio = buscarPartida(idPartida);
        return partidaTorneio.iniciar();
    }

    public boolean registrarResultado(String idPartida, Partida partida) {
        PartidaTorneio partidaTorneio = buscarPartida(idPartida);

        if (partidaTorneio.getStatus() == StatusPartidaTorneio.CONCLUIDA) {
            return false;
        }

        partida.aplicarResultadoNaTabela();
        partidaTorneio.concluir();
        atualizarRodadaAtual();
        return true;
    }

    public boolean isFaseGruposConcluida() {
        return partidasFaseGrupos.stream().allMatch(partida -> partida.getStatus() == StatusPartidaTorneio.CONCLUIDA);
    }

    public List<PartidaTorneio> simularPartidasCpu() {
        int rodadaProcessada = rodadaAtual;
        List<PartidaTorneio> partidasSimuladas = new ArrayList<>();

        for (PartidaTorneio partidaTorneio : getPartidasDaRodada(rodadaProcessada)) {
            if (envolveBrasil(partidaTorneio) || partidaTorneio.getStatus() != StatusPartidaTorneio.AGENDADA) {
                continue;
            }

            Partida partida = iniciarPartida(partidaTorneio.getId());
            simuladorPartidaCpu.simularPartida(partida);
            registrarResultado(partidaTorneio.getId(), partida);
            partidasSimuladas.add(partidaTorneio);
        }

        return List.copyOf(partidasSimuladas);
    }

    public List<Time> getMelhoresTerceiros() {
        return calcularTerceirosOrdenados().stream().limit(8).toList();
    }

    public Map<String, Time> getClassificadosFaseGrupos() {
        Map<String, Time> classificados = new LinkedHashMap<>();
        List<Grupo> gruposOrdenados = grupos.stream().sorted(Comparator.comparing(Grupo::getIdentificador)).toList();

        //Identificando os melhores de cada grupo como X1X2 (X1: Posição; X2: Grupo)
        for (Grupo grupo : gruposOrdenados) {
            List<Time> classificacao = getClassificacaoGrupo(grupo.getIdentificador());
            classificados.put("1" + grupo.getIdentificador(), classificacao.get(0));
            classificados.put("2" + grupo.getIdentificador(), classificacao.get(1));
        }

        //Identificando os melhores terceiros como 3_X (X: Posição entre os terceiros lugares)
        List<Time> melhoresTerceiros = getMelhoresTerceiros();
        for (int i = 0; i < melhoresTerceiros.size(); i++) {
            classificados.put("3_" + (i + 1), melhoresTerceiros.get(i));
        }

        return Collections.unmodifiableMap(classificados); //Retornando a classificação imutável
    }

    private List<Time> calcularTerceirosOrdenados() {
        List<Time> terceiros = new ArrayList<>();
        for (Grupo grupo : grupos) {
            terceiros.add(getClassificacaoGrupo(grupo.getIdentificador()).get(2));
        }

        terceiros.sort(COMPARADOR_CLASSIFICACAO);
        return List.copyOf(terceiros);
    }

    private Grupo buscarGrupo(String identificadorGrupo) {
        String identificadorNormalizado = identificadorGrupo.trim().toUpperCase();
        return grupos.stream().filter(grupo -> grupo.getIdentificador().equals(identificadorNormalizado)).findFirst().orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado: " + identificadorGrupo));
    }

    private PartidaTorneio buscarPartida(String idPartida) {
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

    public List<PartidaTorneio> getPartidasMataMata() {
        return partidasMataMata;
    }

    public FaseTorneio getFaseAtual() {
        return faseAtual;
    }

    public int getRodadaAtual() {
        return rodadaAtual;
    }
}
