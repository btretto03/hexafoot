package hexafoot.ui.view;

import hexafoot.model.EventoPartida;
import hexafoot.model.FaseTorneio;
import hexafoot.model.Jogador;
import hexafoot.model.Partida;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.Time;
import hexafoot.model.strategy.EstrategiaSimulacao;
import hexafoot.model.strategy.TaticaEquilibrada;
import hexafoot.model.strategy.TaticaOfensiva;
import hexafoot.model.strategy.TaticaRetranca;
import hexafoot.service.simulacao.GerenciadorPenaltis;
import hexafoot.service.simulacao.RelogioPartida;
import hexafoot.service.torneio.GerenciadorTorneio;
import hexafoot.ui.GameNavigator;
import hexafoot.ui.TocadorDeSons;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulacaoPartidaView extends TelaBase {
    private static final double TAXA_LENTA = 0.5;
    private static final double TAXA_NORMAL = 3.0;
    private static final double TAXA_RAPIDA = 8.0;
    private static final int MINUTO_INTERVALO = 45;

    private final BorderPane root;
    private final StackPane rootEmpilhado;
    private final PartidaTorneio partidaTorneio;
    private final Partida partida;
    private final RelogioPartida relogio;
    private final boolean brasilEhMandante;
    private final List<Jogador> elencoDoJogador = new ArrayList<>();
    private int minutoAtual = 1;
    private Timeline timeline;
    private boolean jogoEmAndamento = false;
    private boolean substituicaoObrigatoriaPendente = false;
    private boolean intervaloJaExibido = false;
    private StackPane overlayIntervalo;
    private final TocadorDeSons tocadorDeSons = new TocadorDeSons();

    // Rastreamento visual de cartões
    private final Map<String, String> cartoesEmCampo = new HashMap<>();

    // Componentes Visuais Principais
    private Label lblTempo;
    private Label lblPlacarMandante;
    private Label lblPlacarVisitante;

    private VBox containerEventos;
    private VBox containerElencoAoVivo;

    private Button btnPausar;
    private Button btnVelLenta;
    private Button btnVelNormal;
    private Button btnVelRapida;
    private Button btnVoltar;
    private Button btnSegundoTempo;
    private boolean intervaloAtivo = false;

    private VBox painelTecnico;
    private ComboBox<Jogador> comboSai;
    private ComboBox<Jogador> comboEntra;
    private Button btnConfirmarSub;
    private Button btnTaticaOfensiva;
    private Button btnTaticaEquilibrada;
    private Button btnTaticaRetranca;

    // Disputa de penaltis (embutida na propria tela, sem popups)
    private Label lblPlacarPenaltis;
    private Label lblStatusPenaltis;
    private VBox boxEscolhaBatedores;
    private GerenciadorPenaltis gerenciadorPenaltisView;
    private Jogador goleiroMandantePenaltis;
    private Jogador goleiroVisitantePenaltis;
    private List<Jogador> ordemBatedoresCpuPenaltis;
    private final List<Jogador> batedoresJaUsadosMandante = new ArrayList<>();
    private final List<Jogador> batedoresJaUsadosVisitante = new ArrayList<>();
    private int placarPenaltisMandante = 0;
    private int placarPenaltisVisitante = 0;
    private int cobrancasMandante = 0;
    private int cobrancasVisitante = 0;

    public SimulacaoPartidaView(GameNavigator navigator, PartidaTorneio partidaTorneio, Partida partida) {
        super(navigator);
        this.partidaTorneio = partidaTorneio;
        this.partida = partida;
        this.brasilEhMandante = (partida.getMandante() == navigator.getSession().getGerenciadorTorneio().getBrasil());

        Time timeDoJogador = brasilEhMandante ? partida.getMandante() : partida.getVisitante();
        elencoDoJogador.addAll(timeDoJogador.getTitulares());
        elencoDoJogador.addAll(timeDoJogador.getReservas());

        this.relogio = new RelogioPartida();
        this.relogio.adicionarProcessadoresPadrao(timeDoJogador);

        this.root = new BorderPane();
        this.root.getStyleClass().add("screen-root");

        VBox centroLayout = new VBox(20);
        centroLayout.getChildren().addAll(criarPlacar(), criarPainelEventos());
        VBox.setVgrow(centroLayout.getChildren().get(1), Priority.ALWAYS);

        root.setLeft(criarPainelElencoAoVivo());
        root.setCenter(centroLayout);
        root.setRight(criarPainelTecnicoLateral());
        root.setBottom(criarControlesInferiores());

        BorderPane.setMargin(centroLayout, new Insets(24, 12, 12, 12));

        this.rootEmpilhado = new StackPane(root);

        configurarTimeline();
        atualizarEstadoBotoesTempo();
        atualizarEstadoPainelTecnico();
        renderizarElencoAoVivo();

        alterarVelocidade(TAXA_NORMAL); // a partida ja comeca rolando, no ritmo normal
        tocadorDeSons.tocarComecoJogo();
    }

    private VBox criarPlacar() {
        VBox painelPlacar = new VBox(10);
        painelPlacar.getStyleClass().add("hero-panel");
        painelPlacar.setPadding(new Insets(20, 30, 20, 30));
        painelPlacar.setAlignment(Pos.CENTER);

        lblTempo = new Label("0'");
        lblTempo.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #8bf0a1;");

        HBox timesBox = new HBox(30);
        timesBox.setAlignment(Pos.CENTER);

        Label lblMandante = new Label(obterBandeira(partida.getMandante().getNome()) + " " + formatarNomePais(partida.getMandante().getNome()));
        lblMandante.getStyleClass().add("display-title");
        lblMandante.setStyle("-fx-font-size: 32px;");

        lblPlacarMandante = new Label("0");
        lblPlacarMandante.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblX = new Label("X");
        lblX.setStyle("-fx-font-size: 32px; -fx-text-fill: gray;");

        lblPlacarVisitante = new Label("0");
        lblPlacarVisitante.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblVisitante = new Label(obterBandeira(partida.getVisitante().getNome()) + " " + formatarNomePais(partida.getVisitante().getNome()));
        lblVisitante.getStyleClass().add("display-title");
        lblVisitante.setStyle("-fx-font-size: 32px;");

        timesBox.getChildren().addAll(lblMandante, lblPlacarMandante, lblX, lblPlacarVisitante, lblVisitante);

        lblPlacarPenaltis = new Label("");
        lblPlacarPenaltis.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f0d58b;");
        lblPlacarPenaltis.setManaged(false);
        lblPlacarPenaltis.setVisible(false);

        painelPlacar.getChildren().addAll(lblTempo, timesBox, lblPlacarPenaltis);

        return painelPlacar;
    }

    private VBox criarPainelElencoAoVivo() {
        VBox painel = new VBox(10);
        painel.getStyleClass().add("info-card");
        painel.setPadding(new Insets(20));
        painel.setPrefWidth(280);
        BorderPane.setMargin(painel, new Insets(24, 12, 12, 24));

        Label titulo = new Label("Seu Time em Campo");
        titulo.getStyleClass().add("card-title");

        Label subtitulo = new Label("Acompanhe o desgaste e cartões");
        subtitulo.getStyleClass().add("card-text");
        subtitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.5);");

        containerElencoAoVivo = new VBox(6);

        ScrollPane scrollElenco = new ScrollPane(containerElencoAoVivo);
        scrollElenco.getStyleClass().add("championship-scroll");
        scrollElenco.setFitToWidth(true);
        scrollElenco.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        VBox.setVgrow(scrollElenco, Priority.ALWAYS);
        painel.getChildren().addAll(titulo, subtitulo, scrollElenco);

        return painel;
    }

    private VBox criarPainelEventos() {
        VBox painel = new VBox(14);
        painel.getStyleClass().add("info-card");
        painel.setPadding(new Insets(20));

        Label titulo = new Label("Lances da Partida");
        titulo.getStyleClass().add("card-title");

        containerEventos = new VBox(8);

        ScrollPane scrollEventos = new ScrollPane(containerEventos);
        scrollEventos.getStyleClass().add("championship-scroll");
        scrollEventos.setFitToWidth(true);
        scrollEventos.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        VBox.setVgrow(scrollEventos, Priority.ALWAYS);
        painel.getChildren().addAll(titulo, scrollEventos);

        return painel;
    }

    private VBox criarPainelTecnicoLateral() {
        painelTecnico = new VBox(16);
        painelTecnico.getStyleClass().add("info-card");
        painelTecnico.setPadding(new Insets(20));
        painelTecnico.setPrefWidth(320);
        BorderPane.setMargin(painelTecnico, new Insets(24, 24, 12, 12));

        Label tituloPainel = new Label("Área do Técnico");
        tituloPainel.getStyleClass().add("card-title");

        Label subTitulo = new Label("Disponível apenas com o jogo pausado.");
        subTitulo.getStyleClass().add("card-text");
        subTitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.5);");

        Label lblTaticaTitulo = new Label("Postura Estratégica");
        lblTaticaTitulo.getStyleClass().add("pitch-row-title");

        btnTaticaOfensiva = new Button("Pressão");
        btnTaticaOfensiva.setMaxWidth(Double.MAX_VALUE);
        btnTaticaOfensiva.setOnAction(e -> alterarPosturaTatica("ofensiva"));

        btnTaticaEquilibrada = new Button("Posse de Bola");
        btnTaticaEquilibrada.setMaxWidth(Double.MAX_VALUE);
        btnTaticaEquilibrada.setOnAction(e -> alterarPosturaTatica("equilibrada"));

        btnTaticaRetranca = new Button("Retranca");
        btnTaticaRetranca.setMaxWidth(Double.MAX_VALUE);
        btnTaticaRetranca.setOnAction(e -> alterarPosturaTatica("retranca"));

        VBox boxBotoesTatica = new VBox(8, btnTaticaOfensiva, btnTaticaEquilibrada, btnTaticaRetranca);

        int substituicoesFeitas = brasilEhMandante ? partida.getSubstituicoesMandante() : partida.getSubstituicoesVisitante();
        Label lblSubTituloSecao = new Label("Substituições (" + substituicoesFeitas + "/5)");
        lblSubTituloSecao.getStyleClass().add("pitch-row-title");

        comboSai = new ComboBox<>();
        comboSai.setPromptText("Selecione quem sai...");
        comboSai.setMaxWidth(Double.MAX_VALUE);
        configurarFormatacaoCombo(comboSai, false);

        comboEntra = new ComboBox<>();
        comboEntra.setPromptText("Selecione quem entra...");
        comboEntra.setMaxWidth(Double.MAX_VALUE);
        configurarFormatacaoCombo(comboEntra, true);

        btnConfirmarSub = new Button("Confirmar Troca 🔄");
        btnConfirmarSub.getStyleClass().add("primary-button");
        btnConfirmarSub.setMaxWidth(Double.MAX_VALUE);
        btnConfirmarSub.setOnAction(e -> executarSubstituicaoManual());

        VBox boxSubstituicao = new VBox(8, comboSai, comboEntra, btnConfirmarSub);

        painelTecnico.getChildren().addAll(tituloPainel, subTitulo, lblTaticaTitulo, boxBotoesTatica, lblSubTituloSecao, boxSubstituicao);

        atualizarEstiloBotoesTatica();
        return painelTecnico;
    }

    private HBox criarControlesInferiores() {
        HBox controles = new HBox(12);
        controles.setAlignment(Pos.CENTER);
        controles.setPadding(new Insets(12, 24, 24, 24));

        Label lblControle = new Label("Controle da Partida:");
        lblControle.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 0 10 0 0;");

        btnPausar = new Button("⏸ Pausar");
        btnPausar.setOnAction(e -> alterarVelocidade(0));

        btnVelLenta = new Button("Lento");
        btnVelLenta.setOnAction(e -> alterarVelocidade(TAXA_LENTA));

        btnVelNormal = new Button("▶ Normal");
        btnVelNormal.setOnAction(e -> alterarVelocidade(TAXA_NORMAL));

        btnVelRapida = new Button("Rápido");
        btnVelRapida.setOnAction(e -> alterarVelocidade(TAXA_RAPIDA));

        btnSegundoTempo = new Button("Ir para o Segundo Tempo");
        btnSegundoTempo.getStyleClass().add("primary-button");
        btnSegundoTempo.setVisible(false);
        btnSegundoTempo.setManaged(false);
        btnSegundoTempo.setOnAction(e -> iniciarSegundoTempo());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnVoltar = new Button("Voltar ao Hub");
        btnVoltar.getStyleClass().add("ghost-button");
        btnVoltar.setDisable(true);
        btnVoltar.setOnAction(e -> {
            if (timeline != null) timeline.stop();
            navigator.showHub();
        });

        controles.getChildren().addAll(lblControle, btnPausar, btnVelLenta, btnVelNormal, btnVelRapida, btnSegundoTempo, spacer, btnVoltar);
        return controles;
    }

    private void configurarTimeline() {
        KeyFrame frame = new KeyFrame(Duration.millis(800), event -> avancarMinuto());
        timeline = new Timeline(frame);
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void alterarVelocidade(double taxa) {
        if (minutoAtual > 90) return;

        if (intervaloAtivo && taxa != 0) {
            return;
        }

        if (taxa != 0 && substituicaoObrigatoriaPendente) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "Você precisa substituir o jogador lesionado antes de continuar a partida.");
            alerta.initOwner(root.getScene().getWindow());
            alerta.showAndWait();
            return;
        }

        if (taxa == 0) {
            timeline.pause();
            jogoEmAndamento = false;
        } else {
            timeline.setRate(taxa);
            timeline.play();
            jogoEmAndamento = true;
        }

        atualizarEstadoBotoesTempo();
        atualizarEstadoPainelTecnico();
    }

    private void atualizarEstadoBotoesTempo() {
        btnPausar.getStyleClass().setAll(jogoEmAndamento ? "secondary-button" : "primary-button");
        btnVelLenta.getStyleClass().setAll(timeline.getRate() == TAXA_LENTA && jogoEmAndamento ? "primary-button" : "secondary-button");
        btnVelNormal.getStyleClass().setAll(timeline.getRate() == TAXA_NORMAL && jogoEmAndamento ? "primary-button" : "secondary-button");
        btnVelRapida.getStyleClass().setAll(timeline.getRate() == TAXA_RAPIDA && jogoEmAndamento ? "primary-button" : "secondary-button");
    }

    private void avancarMinuto() {
        if (minutoAtual > 90) {
            timeline.stop();
            jogoEmAndamento = false;
            Platform.runLater(this::finalizarPartida);
            return;
        }

        int qtdEventosAntes = partida.getEventos().size();
        relogio.processarMinutoIsolado(minutoAtual, partida);

        lblTempo.setText(minutoAtual + "'");
        lblPlacarMandante.setText(String.valueOf(partida.getGolsMandante()));
        lblPlacarVisitante.setText(String.valueOf(partida.getGolsVisitante()));

        int qtdEventosDepois = partida.getEventos().size();
        boolean jogadorMeuSaiuLesionado = false;
        if (qtdEventosDepois > qtdEventosAntes) {
            List<String> eventosDoMinuto = new ArrayList<>();

            for (int i = qtdEventosAntes; i < qtdEventosDepois; i++) {
                EventoPartida ev = partida.getEventos().get(i);

                if ("Lesao".equals(ev.getTipo()) && elencoDoJogador.contains(ev.getAutor())) {
                    jogadorMeuSaiuLesionado = true;
                }

                tocarSomDoEvento(ev);

                String descricaoCrua = ev.toString();

                if (descricaoCrua.startsWith(ev.getMinuto() + "' - ")) {
                    descricaoCrua = descricaoCrua.substring((ev.getMinuto() + "' - ").length());
                } else if (descricaoCrua.contains(" - ")) {
                    descricaoCrua = descricaoCrua.substring(descricaoCrua.indexOf(" - ") + 3);
                }

                if (eventosDoMinuto.contains(descricaoCrua)) continue;
                eventosDoMinuto.add(descricaoCrua);

                String descLower = descricaoCrua.toLowerCase();
                boolean ignorarEvento = false;
                String tipo = "neutro";

                if (descLower.contains("gol")) tipo = "gol";
                else if (descLower.contains("amarelo")) tipo = "amarelo";
                else if (descLower.contains("vermelho") || descLower.contains("expulso") || descLower.contains("lesão") || descLower.contains("lesao") || descLower.contains("sentiu")) tipo = "perigo";

                Time[] times = { partida.getMandante(), partida.getVisitante() };
                for (Time time : times) {
                    for (Jogador j : time.getTitulares()) {
                        String nomeJ = j.getNome().trim();
                        if (descLower.contains(nomeJ.toLowerCase())) {
                            if (descLower.contains("vermelho") || descLower.contains("expulso")) {
                                if ("Vermelho".equals(cartoesEmCampo.get(nomeJ))) {
                                    ignorarEvento = true;
                                } else {
                                    cartoesEmCampo.put(nomeJ, "Vermelho");
                                    tipo = "perigo";
                                }
                            } else if (descLower.contains("amarelo")) {
                                if ("Vermelho".equals(cartoesEmCampo.get(nomeJ))) {
                                    ignorarEvento = true;
                                } else if ("Amarelo".equals(cartoesEmCampo.get(nomeJ))) {
                                    cartoesEmCampo.put(nomeJ, "Vermelho");
                                    descricaoCrua = descricaoCrua.replaceAll("(?i)cartão amarelo.*", "Cartão Vermelho (2º Amarelo) para " + nomeJ + ".");
                                    tipo = "perigo";
                                } else {
                                    cartoesEmCampo.put(nomeJ, "Amarelo");
                                }
                            }
                        }
                    }
                }

                if (ignorarEvento) continue;

                descricaoCrua = descricaoCrua.replaceAll("(?i)" + partida.getVisitante().getNome(), formatarNomePais(partida.getVisitante().getNome()));
                descricaoCrua = descricaoCrua.replaceAll("(?i)" + partida.getMandante().getNome(), formatarNomePais(partida.getMandante().getNome()));

                containerEventos.getChildren().add(0, criarCardEventoPersonalizado(ev.getMinuto(), descricaoCrua, tipo));
            }
        }

        renderizarElencoAoVivo();

        boolean chegouIntervalo = (minutoAtual == MINUTO_INTERVALO && intervaloJaExibido == false);

        if (jogadorMeuSaiuLesionado) {
            boolean podeSubstituir = brasilEhMandante ? partida.mandantePodeSubstituir() : partida.visitantePodeSubstituir();
            List<Jogador> reservas = brasilEhMandante ? partida.getReservasDisponiveisMandante() : partida.getReservasDisponiveisVisitante();

            if (podeSubstituir && reservas.isEmpty() == false) {
                substituicaoObrigatoriaPendente = true;
                timeline.pause();
                jogoEmAndamento = false;
                containerEventos.getChildren().add(0, criarCardEventoPersonalizado(minutoAtual, "⏸ Faça a substituição do jogador lesionado para continuar.", "info"));
                atualizarEstadoBotoesTempo();
                atualizarEstadoPainelTecnico();
            }
        }

        minutoAtual++;

        // se tem substituicao obrigatoria travando o jogo, o intervalo espera a proxima chamada
        if (chegouIntervalo && substituicaoObrigatoriaPendente == false) {
            intervaloJaExibido = true;
            timeline.pause();
            jogoEmAndamento = false;
            atualizarEstadoBotoesTempo();
            atualizarEstadoPainelTecnico();
            mostrarIntervalo();
        }
    }

    //-----------------Intervalo (aos 45 min)-----------------

    private void mostrarIntervalo() {
        setIntervaloAtivo(true);
        overlayIntervalo = criarOverlayIntervalo();
        rootEmpilhado.getChildren().add(overlayIntervalo);
    }

    private void setIntervaloAtivo(boolean ativo) {
        this.intervaloAtivo = ativo;

        btnPausar.setVisible(!ativo);
        btnPausar.setManaged(!ativo);
        btnVelLenta.setVisible(!ativo);
        btnVelLenta.setManaged(!ativo);
        btnVelNormal.setVisible(!ativo);
        btnVelNormal.setManaged(!ativo);
        btnVelRapida.setVisible(!ativo);
        btnVelRapida.setManaged(!ativo);

        btnSegundoTempo.setVisible(ativo);
        btnSegundoTempo.setManaged(ativo);

        atualizarEstadoBotoesTempo();
        atualizarEstadoPainelTecnico();
    }

    private void fazerAlteracoesIntervalo() {
        if (overlayIntervalo != null) {
            rootEmpilhado.getChildren().remove(overlayIntervalo);
            overlayIntervalo = null;
        }
    }

    private void iniciarSegundoTempo() {
        setIntervaloAtivo(false);
        if (overlayIntervalo != null) {
            rootEmpilhado.getChildren().remove(overlayIntervalo);
            overlayIntervalo = null;
        }
        alterarVelocidade(TAXA_NORMAL);
        tocadorDeSons.tocarComecoJogo(); // apito do segundo tempo
    }

    private StackPane criarOverlayIntervalo() {
        Label titulo = new Label("Intervalo");
        titulo.getStyleClass().add("display-title");

        Label placar = new Label(obterBandeira(partida.getMandante().getNome()) + " " + formatarNomePais(partida.getMandante().getNome())
                + "  " + partida.getGolsMandante() + " x " + partida.getGolsVisitante() + "  "
                + formatarNomePais(partida.getVisitante().getNome()) + " " + obterBandeira(partida.getVisitante().getNome()));
        placar.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fbff;");

        Label dica = new Label("Aproveite para ajustar a tática ou fazer substituições antes do segundo tempo.");
        dica.getStyleClass().add("card-text");
        dica.setWrapText(true);
        dica.setStyle("-fx-text-alignment: center;");

        Button btnFazerAlteracoes = new Button("Fazer Alterações");
        btnFazerAlteracoes.getStyleClass().add("secondary-button");
        btnFazerAlteracoes.setOnAction(e -> fazerAlteracoesIntervalo());

        Button btnIrSegundoTempo = new Button("Ir para o Segundo Tempo");
        btnIrSegundoTempo.getStyleClass().add("primary-button");
        btnIrSegundoTempo.setOnAction(e -> iniciarSegundoTempo());

        HBox botoes = new HBox(12, btnFazerAlteracoes, btnIrSegundoTempo);
        botoes.setAlignment(Pos.CENTER);

        VBox cartao = new VBox(14, titulo, placar, dica, botoes);
        cartao.getStyleClass().add("hero-panel");
        cartao.setAlignment(Pos.CENTER);
        cartao.setPadding(new Insets(36));
        cartao.setMaxWidth(460);
        cartao.setMaxHeight(VBox.USE_PREF_SIZE);

        StackPane overlay = new StackPane(cartao);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.72);");
        return overlay;
    }

    // decide qual efeito sonoro toca de acordo com o tipo do evento gerado pelo motor de simulacao
    private void tocarSomDoEvento(EventoPartida ev) {
        String tipo = ev.getTipo();

        if ("GolMandante".equals(tipo)) {
            if (brasilEhMandante) tocadorDeSons.tocarGol(); else tocadorDeSons.tocarGolAdversario();
        } else if ("GolVisitante".equals(tipo)) {
            if (brasilEhMandante) tocadorDeSons.tocarGolAdversario(); else tocadorDeSons.tocarGol();
        } else if ("CartaoAmarelo".equals(tipo) || "SegundoAmarelo".equals(tipo) || "CartaoVermelho".equals(tipo)) {
            tocadorDeSons.tocarCartao();
        } else if ("Lesao".equals(tipo)) {
            tocadorDeSons.tocarLesao();
        }
    }

    private void renderizarElencoAoVivo() {
        containerElencoAoVivo.getChildren().clear();
        Time timeDoJogador = brasilEhMandante ? partida.getMandante() : partida.getVisitante();
        for (Jogador j : timeDoJogador.getTitulares()) {
            containerElencoAoVivo.getChildren().add(criarCardJogadorVisual(j));
        }
    }

    private void alterarPosturaTatica(String tipo) {
        Time brasil = brasilEhMandante ? partida.getMandante() : partida.getVisitante();
        if (tipo.equals("ofensiva")) brasil.setTaticaAtual(new TaticaOfensiva());
        else if (tipo.equals("equilibrada")) brasil.setTaticaAtual(new TaticaEquilibrada());
        else if (tipo.equals("retranca")) brasil.setTaticaAtual(new TaticaRetranca());

        atualizarEstiloBotoesTatica();
        HBox avisoCard = criarCardEventoPersonalizado(minutoAtual, "📋 Técnico mudou a postura estratégica da equipe.", "info");
        containerEventos.getChildren().add(0, avisoCard);
    }

    private void executarSubstituicaoManual() {
        Jogador sai = comboSai.getSelectionModel().getSelectedItem();
        Jogador entra = comboEntra.getSelectionModel().getSelectedItem();

        if (sai == null || entra == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "Selecione o jogador que vai sair e o que vai entrar.");
            alerta.initOwner(root.getScene().getWindow());
            alerta.showAndWait();
            return;
        }

        boolean sucesso = brasilEhMandante ? partida.substituirMandante(sai, entra) : partida.substituirVisitante(sai, entra);

        if (sucesso) {
            EventoPartida evSub = new EventoPartida(minutoAtual, "Substituicao", sai, entra);
            partida.adicionarEvento(evSub);
            substituicaoObrigatoriaPendente = false;

            String texto = "Substituição: Sai " + sai.getNome() + ", entra " + entra.getNome();
            containerEventos.getChildren().add(0, criarCardEventoPersonalizado(minutoAtual, texto, "neutro"));

            renderizarElencoAoVivo();
            carregarDadosTreinador();
        } else {
            Alert alerta = new Alert(Alert.AlertType.ERROR, "Substituição inválida! Verifique o limite de trocas ou a integridade física do atleta.");
            alerta.initOwner(root.getScene().getWindow());
            alerta.showAndWait();
        }
    }

    private void atualizarEstadoPainelTecnico() {
        boolean desativar = jogoEmAndamento || minutoAtual > 90;
        painelTecnico.setDisable(desativar);

        if (!desativar) {
            carregarDadosTreinador();
        }
    }

    private void carregarDadosTreinador() {
        Time timeDoJogador = brasilEhMandante ? partida.getMandante() : partida.getVisitante();
        List<Jogador> reservasDoJogador = brasilEhMandante ? partida.getReservasDisponiveisMandante() : partida.getReservasDisponiveisVisitante();
        int substituicoesFeitas = brasilEhMandante ? partida.getSubstituicoesMandante() : partida.getSubstituicoesVisitante();
        boolean podeSubstituir = brasilEhMandante ? partida.mandantePodeSubstituir() : partida.visitantePodeSubstituir();

        comboSai.setItems(FXCollections.observableArrayList(timeDoJogador.getTitulares()));
        comboEntra.setItems(FXCollections.observableArrayList(reservasDoJogador));

        Label lblSub = (Label) painelTecnico.getChildren().get(4);
        lblSub.setText("Substituições (" + substituicoesFeitas + "/5)");

        if (!podeSubstituir) {
            btnConfirmarSub.setDisable(true);
            btnConfirmarSub.setText("Limite de Trocas Atingido");
            comboSai.setDisable(true);
            comboEntra.setDisable(true);
        }
    }

    private void atualizarEstiloBotoesTatica() {
        Time timeDoJogador = brasilEhMandante ? partida.getMandante() : partida.getVisitante();
        EstrategiaSimulacao taticaAtual = timeDoJogador.getTaticaAtual();

        btnTaticaOfensiva.getStyleClass().setAll("secondary-button");
        btnTaticaEquilibrada.getStyleClass().setAll("secondary-button");
        btnTaticaRetranca.getStyleClass().setAll("secondary-button");

        if (taticaAtual instanceof TaticaOfensiva) btnTaticaOfensiva.getStyleClass().setAll("primary-button");
        else if (taticaAtual instanceof TaticaEquilibrada) btnTaticaEquilibrada.getStyleClass().setAll("primary-button");
        else if (taticaAtual instanceof TaticaRetranca) btnTaticaRetranca.getStyleClass().setAll("primary-button");
    }

    private HBox criarCardJogadorVisual(Jogador jogador) {
        Label lblPos = new Label(jogador.getPosicao().substring(0, 3).toUpperCase());
        lblPos.getStyleClass().add("status-pill");
        lblPos.setStyle("-fx-min-width: 45px; -fx-alignment: center; -fx-font-size: 10px; -fx-padding: 2px 6px;");

        Label lblNome = new Label(obterNomeCurto(jogador.getNome()));
        lblNome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fbff;");

        HBox layout = new HBox(12, lblPos, lblNome);
        layout.setAlignment(Pos.CENTER_LEFT);

        String statusCartao = cartoesEmCampo.get(jogador.getNome().trim());

        if ("Lesionado".equals(jogador.getStatus())) {
            lblNome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-strikethrough: true;");

            Region spacerLesao = new Region();
            HBox.setHgrow(spacerLesao, Priority.ALWAYS);
            layout.getChildren().add(spacerLesao);

            Label lblLesao = new Label("LES");
            lblLesao.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            layout.getChildren().add(lblLesao);

        } else if ("Vermelho".equals(statusCartao)) {
            lblNome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-strikethrough: true;");

            Label lblCartao = new Label("▮");
            lblCartao.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff6b6b;");
            layout.getChildren().add(lblCartao);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            layout.getChildren().add(spacer);

            Label lblEnergia = new Label("EXP");
            lblEnergia.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            layout.getChildren().add(lblEnergia);

        } else {
            if ("Amarelo".equals(statusCartao)) {
                Label lblCartao = new Label("▮");
                lblCartao.setStyle("-fx-font-size: 14px; -fx-text-fill: #f0d58b;");
                layout.getChildren().add(lblCartao);
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            layout.getChildren().add(spacer);

            int energiaAtual = jogador.getFisico();

            Label lblEnergia = new Label(energiaAtual + "%");
            lblEnergia.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

            if (energiaAtual >= 70) {
                lblEnergia.setStyle(lblEnergia.getStyle() + "-fx-text-fill: #8bf0a1;");
            } else if (energiaAtual >= 40) {
                lblEnergia.setStyle(lblEnergia.getStyle() + "-fx-text-fill: #f0d58b;");
            } else {
                lblEnergia.setStyle(lblEnergia.getStyle() + "-fx-text-fill: #ff6b6b;");
            }
            layout.getChildren().add(lblEnergia);
        }

        return layout;
    }

    private HBox criarCardEventoPersonalizado(int minuto, String descricao, String tipoCard) {
        Label lblMinuto = new Label(minuto + "'");

        Label lblDesc = new Label(descricao);
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #f8f9fa;");

        HBox card = new HBox(12, lblMinuto, lblDesc);
        card.setAlignment(Pos.CENTER_LEFT);

        switch (tipoCard) {
            case "gol":
                card.setStyle("-fx-background-color: rgba(34, 197, 94, 0.15); -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: rgba(34, 197, 94, 0.3); -fx-border-radius: 8;");
                lblMinuto.setStyle("-fx-min-width: 42px; -fx-alignment: center; -fx-font-weight: bold; -fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 4px;");
                lblDesc.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #8bf0a1;");
                break;
            case "amarelo":
                card.setStyle("-fx-background-color: rgba(234, 179, 8, 0.1); -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: rgba(234, 179, 8, 0.2); -fx-border-radius: 8;");
                lblMinuto.setStyle("-fx-min-width: 42px; -fx-alignment: center; -fx-font-weight: bold; -fx-background-color: #eab308; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 4px;");
                lblDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #fef08a;");
                break;
            case "perigo":
                card.setStyle("-fx-background-color: rgba(239, 68, 68, 0.1); -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: rgba(239, 68, 68, 0.3); -fx-border-radius: 8;");
                lblMinuto.setStyle("-fx-min-width: 42px; -fx-alignment: center; -fx-font-weight: bold; -fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 4px;");
                lblDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #fca5a5;");
                break;
            case "info":
                card.setStyle("-fx-background-color: rgba(59, 130, 246, 0.1); -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: rgba(59, 130, 246, 0.2); -fx-border-radius: 8;");
                lblMinuto.setStyle("-fx-min-width: 42px; -fx-alignment: center; -fx-font-weight: bold; -fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 4px;");
                lblDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #bfdbfe;");
                break;
            default: // Neutro
                card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-padding: 10; -fx-background-radius: 8;");
                lblMinuto.setStyle("-fx-min-width: 42px; -fx-alignment: center; -fx-font-weight: bold; -fx-background-color: #334155; -fx-text-fill: #cbd5e1; -fx-background-radius: 6px; -fx-padding: 4px;");
                break;
        }

        return card;
    }

    // INTERCEPTADOR VISUAL DO MENU COMBOBOX: Força o tema escuro na listagem e efeito hover
    private void configurarFormatacaoCombo(ComboBox<Jogador> combo, boolean isReserva) {
        combo.setCellFactory(lv -> new ListCell<Jogador>() {
            @Override
            protected void updateItem(Jogador item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(null);
                    HBox layout = criarCardJogadorVisual(item);

                    Label icone = new Label(isReserva ? "▲" : "▼");
                    icone.setStyle(isReserva ? "-fx-text-fill: #8bf0a1; -fx-font-size: 10px;" : "-fx-text-fill: #ff6b6b; -fx-font-size: 10px;");
                    layout.getChildren().add(0, icone);
                    setGraphic(layout);

                    // Altera o fundo do item dentro da lista aberta para grafite
                    setStyle("-fx-background-color: #111c14; -fx-padding: 6px 12px; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;");

                    // Transições dinâmicas ao passar o rato (hover) para realçar a seleção
                    setOnMouseEntered(e -> setStyle("-fx-background-color: #1e3a27; -fx-padding: 6px 12px; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: #111c14; -fx-padding: 6px 12px; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;"));
                }
            }
        });

        // Célula customizada para quando o seletor estiver fechado (Botão principal do painel)
        combo.setButtonCell(new ListCell<Jogador>() {
            @Override
            protected void updateItem(Jogador item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(combo.getPromptText());
                    setGraphic(null);
                    setStyle("-fx-text-fill: rgba(255,255,255,0.4);");
                } else {
                    setText(null);
                    HBox layout = criarCardJogadorVisual(item);
                    Label icone = new Label(isReserva ? "▲" : "▼");
                    icone.setStyle(isReserva ? "-fx-text-fill: #8bf0a1; -fx-font-size: 10px;" : "-fx-text-fill: #ff6b6b; -fx-font-size: 10px;");
                    layout.getChildren().add(0, icone);
                    setGraphic(layout);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }

    private String formatarNomePais(String nomeBruto) {
        if (nomeBruto == null || nomeBruto.isEmpty()) return "";

        if (nomeBruto.equalsIgnoreCase("africa_do_sul")) return "África do Sul";
        if (nomeBruto.equalsIgnoreCase("coreia_do_sul")) return "Coreia do Sul";
        if (nomeBruto.equalsIgnoreCase("coreia_do_norte")) return "Coreia do Norte";
        if (nomeBruto.equalsIgnoreCase("arabia_saudita")) return "Arábia Saudita";
        if (nomeBruto.equalsIgnoreCase("gra_bretanha")) return "Grã-Bretanha";
        if (nomeBruto.equalsIgnoreCase("camaroes")) return "Camarões";
        if (nomeBruto.equalsIgnoreCase("ira")) return "Irã";
        if (nomeBruto.equalsIgnoreCase("japao")) return "Japão";

        String[] partes = nomeBruto.replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();

        for (String p : partes) {
            if (p.length() > 0) {
                if (p.equalsIgnoreCase("de") || p.equalsIgnoreCase("do") || p.equalsIgnoreCase("da") || p.equalsIgnoreCase("dos") || p.equalsIgnoreCase("das")) {
                    sb.append(p.toLowerCase()).append(" ");
                } else {
                    sb.append(p.substring(0, 1).toUpperCase()).append(p.substring(1).toLowerCase()).append(" ");
                }
            }
        }
        return sb.toString().trim();
    }

    private String obterBandeira(String nomeBruto) {
        if (nomeBruto == null) return "";
        String nome = nomeBruto.trim().toLowerCase();

        if (nome.equals("africa_do_sul")) return "[RSA]";
        if (nome.equals("alemanha")) return "[GER]";
        if (nome.equals("arabia_saudita")) return "[KSA]";
        if (nome.equals("argelia")) return "[ALG]";
        if (nome.equals("argentina")) return "[ARG]";
        if (nome.equals("australia")) return "[AUS]";
        if (nome.equals("austria")) return "[AUT]";
        if (nome.equals("belgica")) return "[BEL]";
        if (nome.equals("bosnia_e_herzegovina")) return "[BIH]";
        if (nome.equals("brasil")) return "[BRA]";
        if (nome.equals("cabo_verde")) return "[CPV]";
        if (nome.equals("camaroes")) return "[CMR]";
        if (nome.equals("canada")) return "[CAN]";
        if (nome.equals("catar")) return "[QAT]";
        if (nome.equals("colombia")) return "[COL]";
        if (nome.equals("coreia_do_sul")) return "[KOR]";
        if (nome.equals("costa_do_marfim")) return "[CIV]";
        if (nome.equals("croacia")) return "[CRO]";
        if (nome.equals("curacau")) return "[CUW]";
        if (nome.equals("dinamarca")) return "[DEN]";
        if (nome.equals("egito")) return "[EGY]";
        if (nome.equals("equador")) return "[ECU]";
        if (nome.equals("escocia")) return "[SCO]";
        if (nome.equals("espanha")) return "[ESP]";
        if (nome.equals("estados_unidos")) return "[USA]";
        if (nome.equals("franca")) return "[FRA]";
        if (nome.equals("gana")) return "[GHA]";
        if (nome.equals("haiti")) return "[HAI]";
        if (nome.equals("holanda")) return "[NED]";
        if (nome.equals("inglaterra")) return "[ENG]";
        if (nome.equals("ira")) return "[IRN]";
        if (nome.equals("iraque")) return "[IRQ]";
        if (nome.equals("japao")) return "[JPN]";
        if (nome.equals("jordania")) return "[JOR]";
        if (nome.equals("marrocos")) return "[MAR]";
        if (nome.equals("mexico")) return "[MEX]";
        if (nome.equals("noruega")) return "[NOR]";
        if (nome.equals("nova_zelandia")) return "[NZL]";
        if (nome.equals("panama")) return "[PAN]";
        if (nome.equals("paraguai")) return "[PAR]";
        if (nome.equals("portugal")) return "[POR]";
        if (nome.equals("republica_democratica_do_congo")) return "[COD]";
        if (nome.equals("republica_tcheca")) return "[CZE]";
        if (nome.equals("senegal")) return "[SEN]";
        if (nome.equals("suecia")) return "[SWE]";
        if (nome.equals("suica")) return "[SUI]";
        if (nome.equals("tunisia")) return "[TUN]";
        if (nome.equals("turquia")) return "[TUR]";
        if (nome.equals("uruguai")) return "[URU]";
        if (nome.equals("uzbequistao")) return "[UZB]";

        return "[INT]";
    }

    private String obterNomeCurto(String nomeCompleto) {
        if (nomeCompleto == null) return "";
        String[] partes = nomeCompleto.split(" ");
        if (partes.length > 1) {
            return partes[0] + " " + partes[partes.length - 1].charAt(0) + ".";
        }
        return partes[0];
    }

    private void finalizarPartida() {
        timeline.stop();
        jogoEmAndamento = false;
        GerenciadorTorneio gerenciadorTorneio = navigator.getSession().getGerenciadorTorneio();

        if (partidaTorneio.getFase() == FaseTorneio.FASE_DE_GRUPOS) {
            gerenciadorTorneio.registrarResultado(partidaTorneio.getId(), partida);
            gerenciadorTorneio.simularPartidasCpu();

            if (gerenciadorTorneio.isFaseGruposConcluida()) {
                gerenciadorTorneio.iniciarMataMata();
                gerenciadorTorneio.simularAteProximaPartidaBrasilOuFim();
            }

            exibirResultadoFinal();
            return;
        }

        if (partida.getGolsMandante() == partida.getGolsVisitante()) {
            iniciarDisputaPenaltis(); //empatou, o jogo so termina de verdade quando a disputa acabar
            return;
        }

        Time vencedor = partida.getGolsMandante() > partida.getGolsVisitante() ? partida.getMandante() : partida.getVisitante();
        gerenciadorTorneio.registrarResultadoMataMata(partidaTorneio.getId(), partida, vencedor);
        gerenciadorTorneio.simularAteProximaPartidaBrasilOuFim();

        exibirResultadoFinal();
    }

    private void exibirResultadoFinal() {
        tocadorDeSons.tocarFimDeJogo();
        lblTempo.setText("FIM");
        atualizarEstadoBotoesTempo();
        btnPausar.setText("Encerrada");
        btnPausar.setDisable(true);
        btnVelLenta.setDisable(true);
        btnVelNormal.setDisable(true);
        btnVelRapida.setDisable(true);
        btnVoltar.setDisable(false);

        painelTecnico.setDisable(true);

        String textoFim = "🏁 FIM DE JOGO! " + obterBandeira(partida.getMandante().getNome()) + " " + formatarNomePais(partida.getMandante().getNome()) + " " + partida.getGolsMandante() + " x " + partida.getGolsVisitante() + " " + formatarNomePais(partida.getVisitante().getNome()) + " " + obterBandeira(partida.getVisitante().getNome());
        HBox fimCard = criarCardEventoPersonalizado(90, textoFim, "info");
        containerEventos.getChildren().add(0, fimCard);

        GerenciadorTorneio gerenciadorTorneio = navigator.getSession().getGerenciadorTorneio();
        if (gerenciadorTorneio.campanhaBrasilEncerrada()) { //campanha do Brasil acabou (eliminado ou campeao)
            containerEventos.getChildren().add(0, criarCardResultadoCampanha(gerenciadorTorneio.getResultadoFinalBrasil()));
        }
    }

    // monta o aviso de fim de jogo (eliminado/campeão...)
    private HBox criarCardResultadoCampanha(String resultado) {
        String texto;
        String tipo;

        switch (resultado) {
            case "CAMPEAO":
                texto = "🏆 CAMPEÃO DO MUNDO! O Brasil conquistou a Copa de 2026!";
                tipo = "gol";
                break;
            case "VICE_CAMPEAO":
                texto = "🥈 Vice-campeão! O Brasil perdeu a final, mas fez uma grande campanha.";
                tipo = "info";
                break;
            case "TERCEIRO_LUGAR":
                texto = "🥉 Terceiro lugar! O Brasil fecha a Copa no pódio.";
                tipo = "info";
                break;
            case "QUARTO_LUGAR":
                texto = "4º lugar. O Brasil não conseguiu o pódio dessa vez.";
                tipo = "amarelo";
                break;
            default:
                texto = "❌ Eliminado(a) " + nomeDaFaseEliminacao(resultado) + ". A campanha do Brasil na Copa termina por aqui.";
                tipo = "perigo";
                break;
        }

        return criarCardEventoPersonalizado(90, texto, tipo);
    }

    private String nomeDaFaseEliminacao(String resultado) {
        if (resultado.equals("ELIMINADO_FASE_DE_GRUPOS")) return "na fase de grupos";
        if (resultado.equals("ELIMINADO_DEZESSEIS_AVOS")) return "nos dezesseis-avos de final";
        if (resultado.equals("ELIMINADO_OITAVAS")) return "nas oitavas de final";
        if (resultado.equals("ELIMINADO_QUARTAS")) return "nas quartas de final";
        if (resultado.equals("ELIMINADO_SEMIFINAL")) return "na semifinal";
        return "no mata-mata";
    }

    //-----------------Disputa de penaltis, embutida na tela principal -----------------

    private void iniciarDisputaPenaltis() {
        gerenciadorPenaltisView = new GerenciadorPenaltis();
        goleiroMandantePenaltis = gerenciadorPenaltisView.obterGoleiro(partida.getMandante());
        goleiroVisitantePenaltis = gerenciadorPenaltisView.obterGoleiro(partida.getVisitante());

        Time oponente = brasilEhMandante ? partida.getVisitante() : partida.getMandante();
        ordemBatedoresCpuPenaltis = gerenciadorPenaltisView.definirOrdemDosBatedores(oponente);

        placarPenaltisMandante = 0;
        placarPenaltisVisitante = 0;
        cobrancasMandante = 0;
        cobrancasVisitante = 0;
        batedoresJaUsadosMandante.clear();
        batedoresJaUsadosVisitante.clear();

        lblTempo.setText("PÊNALTIS");
        lblPlacarPenaltis.setManaged(true);
        lblPlacarPenaltis.setVisible(true);
        atualizarPlacarPenaltis();

        btnPausar.setText("Pênaltis em andamento");
        btnPausar.setDisable(true);
        btnVelLenta.setDisable(true);
        btnVelNormal.setDisable(true);
        btnVelRapida.setDisable(true);

        containerEventos.getChildren().add(0, criarCardEventoPersonalizado(120, "⚖ Empate no tempo normal! A decisão vai para os pênaltis.", "info"));

        montarPainelPenaltis();
        prepararProximaCobranca();
    }

    private void montarPainelPenaltis() {
        Label titulo = new Label("Disputa de Pênaltis");
        titulo.getStyleClass().add("card-title");

        lblStatusPenaltis = new Label("Preparando a disputa...");
        lblStatusPenaltis.getStyleClass().add("card-text");
        lblStatusPenaltis.setWrapText(true);

        boxEscolhaBatedores = new VBox(8);

        painelTecnico.setDisable(false);
        painelTecnico.getChildren().setAll(titulo, lblStatusPenaltis, boxEscolhaBatedores);
    }

    private void prepararProximaCobranca() {
        boolean turnoMandante = cobrancasMandante == cobrancasVisitante;
        boolean turnoBrasil = turnoMandante == brasilEhMandante;

        boxEscolhaBatedores.getChildren().clear();

        if (turnoBrasil) {
            exibirEscolhaDeBatedor(turnoMandante);
        } else {
            int indiceCobranca = turnoMandante ? cobrancasMandante : cobrancasVisitante;
            Jogador batedor = ordemBatedoresCpuPenaltis.get(indiceCobranca % ordemBatedoresCpuPenaltis.size());
            lblStatusPenaltis.setText("Vez do adversário cobrar...");
            executarCobranca(batedor, turnoMandante);
        }
    }

    private void exibirEscolhaDeBatedor(boolean turnoMandante) {
        Time timeBrasil = turnoMandante ? partida.getMandante() : partida.getVisitante();
        List<Jogador> jaUsados = turnoMandante ? batedoresJaUsadosMandante : batedoresJaUsadosVisitante;

        List<Jogador> elegiveis = new ArrayList<>();
        for (Jogador jogador : timeBrasil.getTitulares()) {
            if ("Ativo".equals(jogador.getStatus()) && jaUsados.contains(jogador) == false) {
                elegiveis.add(jogador);
            }
        }

        if (elegiveis.isEmpty()) { //todo mundo elegivel ja bateu nessa serie, libera todos de novo pra rodada seguinte
            jaUsados.clear();
            for (Jogador jogador : timeBrasil.getTitulares()) {
                if ("Ativo".equals(jogador.getStatus())) {
                    elegiveis.add(jogador);
                }
            }
        }

        if (elegiveis.isEmpty()) { //caso extremo: ninguem ativo, poe o primeiro titular mesmo assim pra nao travar
            elegiveis.add(timeBrasil.getTitulares().get(0));
        }

        lblStatusPenaltis.setText("Escolha quem vai bater:");

        for (Jogador jogador : elegiveis) {
            Button botao = new Button(jogador.getNome() + " (" + jogador.getPosicao() + ")");
            botao.getStyleClass().add("secondary-button");
            botao.setMaxWidth(Double.MAX_VALUE);
            botao.setOnAction(e -> executarCobranca(jogador, turnoMandante));
            boxEscolhaBatedores.getChildren().add(botao);
        }
    }

    private void executarCobranca(Jogador batedor, boolean turnoMandante) {
        boxEscolhaBatedores.getChildren().clear();
        lblStatusPenaltis.setText("🎯 " + batedor.getNome() + " vai cobrar...");

        if (turnoMandante) {
            batedoresJaUsadosMandante.add(batedor);
        } else {
            batedoresJaUsadosVisitante.add(batedor);
        }

        PauseTransition tensao = new PauseTransition(Duration.millis(1100)); //contador de tensao antes de revelar o resultado
        tensao.setOnFinished(evento -> revelarResultadoCobranca(batedor, turnoMandante));
        tensao.play();
    }

    private void revelarResultadoCobranca(Jogador batedor, boolean turnoMandante) {
        Jogador goleiroAdversario = turnoMandante ? goleiroVisitantePenaltis : goleiroMandantePenaltis;
        String lado = turnoMandante ? "Mandante" : "Visitante";
        boolean convertido = gerenciadorPenaltisView.realizarCobranca(batedor, goleiroAdversario, partida, lado);

        if (turnoMandante) {
            cobrancasMandante++;
            if (convertido) placarPenaltisMandante++;
        } else {
            cobrancasVisitante++;
            if (convertido) placarPenaltisVisitante++;
        }

        Time timeQueBateu = turnoMandante ? partida.getMandante() : partida.getVisitante();
        String texto = (convertido ? "⚽ GOL! " : "❌ Perdeu! ") + batedor.getNome() + " (" + formatarNomePais(timeQueBateu.getNome()) + ")";
        containerEventos.getChildren().add(0, criarCardEventoPersonalizado(120, texto, convertido ? "gol" : "perigo"));

        lblStatusPenaltis.setText(convertido ? "⚽ Gol de " + batedor.getNome() + "!" : "❌ " + batedor.getNome() + " perdeu a cobrança.");
        atualizarPlacarPenaltis();

        PauseTransition proxima = new PauseTransition(Duration.millis(700));
        if (penaltisDecidido()) {
            proxima.setOnFinished(evento -> finalizarDisputaPenaltis());
        } else {
            proxima.setOnFinished(evento -> prepararProximaCobranca());
        }
        proxima.play();
    }

    private boolean penaltisDecidido() {
        if (cobrancasMandante < 5 || cobrancasVisitante < 5) {
            int restantesMandante = 5 - cobrancasMandante;
            int restantesVisitante = 5 - cobrancasVisitante;
            return gerenciadorPenaltisView.matematicamenteDecidido(placarPenaltisMandante, placarPenaltisVisitante, restantesMandante, restantesVisitante);
        }

        //fase de morte subita: so decide quando os dois ja bateram na rodada e o placar mudou
        return cobrancasMandante == cobrancasVisitante && placarPenaltisMandante != placarPenaltisVisitante;
    }

    private void atualizarPlacarPenaltis() {
        String nomeMandante = formatarNomePais(partida.getMandante().getNome());
        String nomeVisitante = formatarNomePais(partida.getVisitante().getNome());
        lblPlacarPenaltis.setText("Pênaltis: " + nomeMandante + " " + placarPenaltisMandante + " x " + placarPenaltisVisitante + " " + nomeVisitante);
    }

    private void finalizarDisputaPenaltis() {
        Time vencedor = placarPenaltisMandante > placarPenaltisVisitante ? partida.getMandante() : partida.getVisitante();

        lblStatusPenaltis.setText("🏆 " + formatarNomePais(vencedor.getNome()) + " venceu nos pênaltis!");
        boxEscolhaBatedores.getChildren().clear();

        GerenciadorTorneio gerenciadorTorneio = navigator.getSession().getGerenciadorTorneio();
        gerenciadorTorneio.registrarResultadoMataMata(partidaTorneio.getId(), partida, vencedor);
        gerenciadorTorneio.simularAteProximaPartidaBrasilOuFim();

        exibirResultadoFinal();
    }

    @Override
    public Parent getRoot() {
        return rootEmpilhado;
    }
}
