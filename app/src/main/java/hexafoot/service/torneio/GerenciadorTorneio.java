package hexafoot.service.torneio;

import hexafoot.dados.FabricaTorneio;
import hexafoot.model.FaseTorneio;
import hexafoot.model.Grupo;
import hexafoot.model.Partida;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.StatusPartidaTorneio;
import hexafoot.model.Time;
import hexafoot.service.simulacao.GerenciadorPenaltis;
import hexafoot.service.simulacao.GerenciadorPosJogo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Coordena calendário, classificação, simulações da CPU e progressão do chaveamento.
 * As equipes são mantidas pelas mesmas instâncias recebidas; registrar resultados
 * altera suas estatísticas e o estado de seus jogadores.
 */
public class GerenciadorTorneio implements Serializable {

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
    private final GerenciadorPenaltis gerenciadorPenaltis;
    private final GerenciadorPosJogo gerenciadorPosJogo;
    private FaseTorneio faseAtual;
    private int rodadaAtual;

    /**
     * Monta os grupos e calendários definidos nos dados do torneio e inicia a
     * primeira rodada da fase de grupos.
     *
     * @param brasil seleção controlada pelo jogador
     * @param selecoesInternacionais demais seleções, sem incluir o Brasil
     */
    public GerenciadorTorneio(Time brasil, List<Time> selecoesInternacionais) {
        this.brasil = brasil;
        List<Time> todasSelecoes = new ArrayList<>(selecoesInternacionais);
        todasSelecoes.add(brasil);
        FabricaTorneio fabricaTorneio = new FabricaTorneio();
        this.grupos = fabricaTorneio.montarGrupos(todasSelecoes);
        this.partidasFaseGrupos = fabricaTorneio.montarCalendarioFaseGrupos(grupos);
        this.partidasMataMata = fabricaTorneio.montarChaveamentoMataMata();
        this.simuladorPartidaCpu = new SimuladorPartidaCpu();
        this.gerenciadorPenaltis = new GerenciadorPenaltis();
        this.gerenciadorPosJogo = new GerenciadorPosJogo();
        this.faseAtual = FaseTorneio.FASE_DE_GRUPOS;
        this.rodadaAtual = 1;
    }

    /**
     * Produz uma classificação imutável por pontos, saldo de gols, gols marcados
     * e nome, nessa ordem.
     *
     * @param identificadorGrupo letra do grupo, sem distinção entre maiúsculas e minúsculas
     * @throws IllegalArgumentException se o grupo não existir
     */
    public List<Time> getClassificacaoGrupo(String identificadorGrupo) {
        List<Time> classificacao = new ArrayList<>(buscarGrupo(identificadorGrupo).getTimes());
        classificacao.sort(COMPARADOR_CLASSIFICACAO);
        return List.copyOf(classificacao);
    }

    /**
     * @return visão imutável das partidas da rodada informada
     */
    public List<PartidaTorneio> getPartidasDaRodada(int rodada) {
        return partidasFaseGrupos.stream().filter(partida -> Integer.valueOf(rodada).equals(partida.getRodada())).toList();
    }

    /**
     * Busca a primeira partida agendada do Brasil na rodada atual da fase de grupos
     * ou na fase eliminatória atual.
     */
    public Optional<PartidaTorneio> getProximaPartidaBrasil() {
        if (faseAtual == FaseTorneio.FASE_DE_GRUPOS) {
            return partidasFaseGrupos.stream().filter(partida -> Integer.valueOf(rodadaAtual).equals(partida.getRodada()))
                                    .filter(partida -> partida.getStatus() == StatusPartidaTorneio.AGENDADA)
                                    .filter(this::envolveBrasil).findFirst();
        }

        return getPartidasFaseAtual().stream().filter(partida -> partida.getStatus() == StatusPartidaTorneio.AGENDADA)
                                .filter(this::envolveBrasil).findFirst();
    }

    /**
     * Cria e associa uma nova simulação à partida do calendário, alterando seu
     * status para {@code EM_ANDAMENTO}.
     *
     * @throws IllegalArgumentException se o identificador não existir
     */
    public Partida iniciarPartida(String idPartida) {
        PartidaTorneio partidaTorneio = buscarPartida(idPartida);
        return partidaTorneio.iniciar();
    }

    /**
     * Registra uma partida da fase de grupos: aplica placar e pontos à tabela,
     * processa o pós-jogo, conclui a partida e avança a rodada quando possível.
     *
     * @return {@code false} se a partida já havia sido concluída; {@code true} se
     *         o resultado foi aplicado
     * @throws IllegalArgumentException se o identificador não for da fase de grupos
     */
    public boolean registrarResultado(String idPartida, Partida partida) {
        PartidaTorneio partidaTorneio = buscarPartidaFaseGrupos(idPartida);

        if (partidaTorneio.getStatus() == StatusPartidaTorneio.CONCLUIDA) {
            return false;
        }

        partida.aplicarResultadoNaTabela();
        aplicarConsequenciasPosJogo(partida);
        partidaTorneio.concluir();
        atualizarRodadaAtual();
        return true;
    }

    /**
     * Registra uma partida eliminatória, propaga vencedor e perdedor no chaveamento
     * e avança a fase quando todos os confrontos atuais terminarem. O resultado não
     * é aplicado à tabela da fase de grupos.
     *
     * @param vencedorDesempate vencedor dos pênaltis quando o placar estiver empatado;
     *                          deve ser {@code null} apenas se houver vencedor no tempo normal
     * @return {@code false} se a partida já havia sido concluída; {@code true} se
     *         o resultado foi aplicado
     * @throws IllegalArgumentException se o identificador não for do mata-mata
     */
    public boolean registrarResultadoMataMata(String idPartida, Partida partida, Time vencedorDesempate) {
        PartidaTorneio partidaTorneio = buscarPartidaMataMata(idPartida);

        if (partidaTorneio.getStatus() == StatusPartidaTorneio.CONCLUIDA) {
            return false;
        }

        Time vencedor;

        if (partida.getGolsMandante() > partida.getGolsVisitante()) {
            vencedor = partida.getMandante();
        } else if (partida.getGolsVisitante() > partida.getGolsMandante()) {
            vencedor = partida.getVisitante();
        } else {
            vencedor = vencedorDesempate;
        }

        aplicarConsequenciasPosJogo(partida);
        partidaTorneio.concluir(vencedor);
        propagarResultado(partidaTorneio);
        atualizarFaseMataMata();
        return true;
    }

    /**
     * @return {@code true} quando todas as partidas de todas as rodadas estiverem concluídas
     */
    public boolean isFaseGruposConcluida() {
        return partidasFaseGrupos.stream().allMatch(partida -> partida.getStatus() == StatusPartidaTorneio.CONCLUIDA);
    }

    /**
     * Simula as partidas agendadas que não envolvem o Brasil na rodada ou fase atual.
     * Empates eliminatórios são decididos automaticamente por pênaltis. Cada resultado
     * é registrado e pode avançar o torneio.
     *
     * @return lista imutável das partidas simuladas nesta chamada
     */
    public List<PartidaTorneio> simularPartidasCpu() {
        if (faseAtual == FaseTorneio.FASE_DE_GRUPOS) {
            return simularPartidasCpuFaseDeGrupos();
        }

        return simularPartidasCpuMataMata();
    }

    /**
     * Fora da fase de grupos, simula fases sucessivas enquanto não houver partida
     * agendada do Brasil. Encerra ao encontrar essa partida, ao terminar o torneio
     * ou quando nenhuma simulação puder progredir.
     */
    public void simularAteProximaPartidaBrasilOuFim() {
        while (faseAtual != FaseTorneio.FASE_DE_GRUPOS && faseAtual != FaseTorneio.ENCERRADO && getProximaPartidaBrasil().isEmpty()) {
            List<PartidaTorneio> partidasSimuladas = simularPartidasCpu();

            if (partidasSimuladas.isEmpty()) {
                return;
            }
        }
    }

    private List<PartidaTorneio> simularPartidasCpuFaseDeGrupos() {
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

    private List<PartidaTorneio> simularPartidasCpuMataMata() {
        List<PartidaTorneio> partidasDaFase = getPartidasFaseAtual();
        List<PartidaTorneio> partidasSimuladas = new ArrayList<>();

        for (PartidaTorneio partidaTorneio : partidasDaFase) {
            if (envolveBrasil(partidaTorneio) || partidaTorneio.getStatus() != StatusPartidaTorneio.AGENDADA) {
                continue;
            }

            Partida partida = iniciarPartida(partidaTorneio.getId());
            simuladorPartidaCpu.simularPartida(partida);

            Time vencedorDesempate = null;
            if (gerenciadorPenaltis.verificarNecessidadeDeDesempate(partida, true)) {
                vencedorDesempate = gerenciadorPenaltis.disputarDecisaoPorPenaltis(partida, List.of());
            }

            registrarResultadoMataMata(partidaTorneio.getId(), partida, vencedorDesempate);
            partidasSimuladas.add(partidaTorneio);
        }

        return List.copyOf(partidasSimuladas);
    }

    /**
     * Seleciona os oito melhores terceiros colocados usando os mesmos critérios
     * de desempate da classificação dos grupos.
     *
     * @return lista imutável, ordenada do melhor para o pior
     */
    public List<Time> getMelhoresTerceiros() {
        return calcularTerceirosOrdenados().stream().limit(8).toList();
    }

    /**
     * Mapeia primeiros e segundos colocados pelas chaves {@code 1A}, {@code 2A},
     * etc., e os melhores terceiros por {@code 3_1} até {@code 3_8}.
     *
     * @return mapa imutável na ordem dos grupos e, depois, dos melhores terceiros
     */
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

    /**
     * Preenche os participantes dos dezesseis-avos conforme as chaves de origem e
     * muda a fase atual. Pressupõe que a fase de grupos já esteja concluída.
     */
    public void iniciarMataMata() {
        Map<String, Time> classificados = getClassificadosFaseGrupos();

        for (PartidaTorneio partida : partidasMataMata) {
            if (partida.getFase() == FaseTorneio.DEZESSEIS_AVOS) {
                Time mandante = classificados.get(partida.getIdentificadorOrigemMandante());
                Time visitante = classificados.get(partida.getIdentificadorOrigemVisitante());
                partida.definirParticipantes(mandante, visitante);
            }
        }

        faseAtual = FaseTorneio.DEZESSEIS_AVOS;
    }

    /**
     * @return lista imutável da rodada atual, na fase de grupos, ou de todos os
     *         confrontos da fase eliminatória atual
     */
    public List<PartidaTorneio> getPartidasFaseAtual() {
        if (faseAtual == FaseTorneio.FASE_DE_GRUPOS) {
            return getPartidasDaRodada(rodadaAtual);
        }

        List<PartidaTorneio> partidas = new ArrayList<>();
        for (PartidaTorneio partida : partidasMataMata) {
            if (partida.getFase() == faseAtual) {
                partidas.add(partida);
            }
        }

        return List.copyOf(partidas);
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
        for (PartidaTorneio partida : partidasFaseGrupos) {
            if (partida.getId().equals(idPartida.trim())) {
                return partida;
            }
        }

        return buscarPartidaMataMata(idPartida);
    }

    private PartidaTorneio buscarPartidaFaseGrupos(String idPartida) {
        return partidasFaseGrupos.stream().filter(partida -> partida.getId().equals(idPartida.trim())).findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Partida da fase de grupos não encontrada: " + idPartida));
    }

    private PartidaTorneio buscarPartidaMataMata(String idPartida) {
        return partidasMataMata.stream().filter(partida -> partida.getId().equals(idPartida.trim())).findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Partida do mata-mata não encontrada: " + idPartida));
    }

    /**
     * Resolve as referências {@code Vencedor_<id>} e {@code Perdedor_<id>} das
     * partidas posteriores usando o resultado recém-concluído.
     */
    private void propagarResultado(PartidaTorneio partidaConcluida) {
        String origemVencedor = "Vencedor_" + partidaConcluida.getId();
        String origemPerdedor = "Perdedor_" + partidaConcluida.getId();

        for (PartidaTorneio proximaPartida : partidasMataMata) {
            if (origemVencedor.equals(proximaPartida.getIdentificadorOrigemMandante())) {
                proximaPartida.definirParticipantes(partidaConcluida.getVencedor(), proximaPartida.getVisitante());
            }
            if (origemVencedor.equals(proximaPartida.getIdentificadorOrigemVisitante())) {
                proximaPartida.definirParticipantes(proximaPartida.getMandante(), partidaConcluida.getVencedor());
            }
            if (origemPerdedor.equals(proximaPartida.getIdentificadorOrigemMandante())) {
                proximaPartida.definirParticipantes(partidaConcluida.getPerdedor(), proximaPartida.getVisitante());
            }
            if (origemPerdedor.equals(proximaPartida.getIdentificadorOrigemVisitante())) {
                proximaPartida.definirParticipantes(proximaPartida.getMandante(), partidaConcluida.getPerdedor());
            }
        }
    }

    /**
     * Avança somente após a conclusão de toda a fase atual. Ao entrar na semifinal,
     * zera os amarelos das equipes já propagadas para essa fase.
     */
    private void atualizarFaseMataMata() {
        for (PartidaTorneio partida : getPartidasFaseAtual()) {
            if (partida.getStatus() != StatusPartidaTorneio.CONCLUIDA) {
                return;
            }
        }

        if (faseAtual == FaseTorneio.DEZESSEIS_AVOS) {
            faseAtual = FaseTorneio.OITAVAS;
        } else if (faseAtual == FaseTorneio.OITAVAS) {
            faseAtual = FaseTorneio.QUARTAS;
        } else if (faseAtual == FaseTorneio.QUARTAS) {
            faseAtual = FaseTorneio.SEMIFINAL;
            limparCartoesDaFase(faseAtual); //na copa os cartoes amarelos zeram ao chegar na semifinal
        } else if (faseAtual == FaseTorneio.SEMIFINAL) {
            faseAtual = FaseTorneio.TERCEIRO_LUGAR;
        } else if (faseAtual == FaseTorneio.TERCEIRO_LUGAR) {
            faseAtual = FaseTorneio.FINAL;
        } else if (faseAtual == FaseTorneio.FINAL) {
            faseAtual = FaseTorneio.ENCERRADO;
        }
    }

    //-----------------Consequencias pos-jogo (desgaste, recuperacao, lesao e suspensao)-----------------
    /**
     * Processa cartões, avança uma rodada dos afastamentos e recupera a energia
     * de titulares e reservas das duas equipes, nessa ordem.
     */
    private void aplicarConsequenciasPosJogo(Partida partida) {
        Time mandante = partida.getMandante();
        Time visitante = partida.getVisitante();

        gerenciadorPosJogo.processarCartoesAcumulados(mandante, partida);
        gerenciadorPosJogo.processarCartoesAcumulados(visitante, partida);

        gerenciadorPosJogo.atualizarStatusLesao(mandante);
        gerenciadorPosJogo.atualizarStatusLesao(visitante);

        gerenciadorPosJogo.regenerarFisicoElenco(mandante);
        gerenciadorPosJogo.regenerarFisicoElenco(visitante);
    }

    /**
     * Zera os amarelos de todas as equipes já definidas para a fase informada.
     */
    private void limparCartoesDaFase(FaseTorneio fase) {
        for (PartidaTorneio partida : partidasMataMata) {
            if (partida.getFase() != fase) {
                continue;
            }
            if (partida.getMandante() != null) {
                gerenciadorPosJogo.limparCartoesFaseAvancada(partida.getMandante());
            }
            if (partida.getVisitante() != null) {
                gerenciadorPosJogo.limparCartoesFaseAvancada(partida.getVisitante());
            }
        }
    }

    /**
     * Avança até a terceira rodada quando todas as partidas da rodada atual terminarem.
     */
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

    public Time getCampeao() {
        return buscarPartidaMataMata("M32").getVencedor();
    }

    public Time getTerceiroColocado() {
        return buscarPartidaMataMata("M31").getVencedor();
    }

    //-----------------Resultado final da campanha do Brasil-----------------
    /**
     * Indica o encerramento de todo o torneio, não apenas uma eventual eliminação
     * antecipada do Brasil.
     *
     * @return {@code true} somente na fase {@code ENCERRADO}
     */
    public boolean campanhaBrasilEncerrada() {
        return faseAtual == FaseTorneio.ENCERRADO;
    }

    /**
     * Resume a colocação do Brasil após o torneio, distinguindo campeão, vice,
     * terceiro, quarto e a fase da eliminação.
     *
     * @return um código estável como {@code CAMPEAO}, {@code VICE_CAMPEAO},
     *         {@code TERCEIRO_LUGAR}, {@code QUARTO_LUGAR} ou
     *         {@code ELIMINADO_<FASE>}
     */
    public String getResultadoFinalBrasil() {
        if (getCampeao() == brasil) {
            return "CAMPEAO";
        }

        PartidaTorneio finalDaCopa = buscarPartidaMataMata("M32");
        if (finalDaCopa.getPerdedor() == brasil) {
            return "VICE_CAMPEAO";
        }

        if (getTerceiroColocado() == brasil) {
            return "TERCEIRO_LUGAR";
        }

        PartidaTorneio disputaTerceiroLugar = buscarPartidaMataMata("M31");
        if (disputaTerceiroLugar.getPerdedor() == brasil) {
            return "QUARTO_LUGAR";
        }

        for (PartidaTorneio partida : partidasMataMata) {
            if (partida.getPerdedor() == brasil) {
                return "ELIMINADO_" + partida.getFase().name();
            }
        }

        return "ELIMINADO_FASE_DE_GRUPOS"; //nao se classificou para o mata-mata
    }
}
