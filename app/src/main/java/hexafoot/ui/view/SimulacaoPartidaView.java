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
import hexafoot.service.simulacao.RelogioPartida;
import hexafoot.service.torneio.GerenciadorTorneio;
import hexafoot.ui.GameNavigator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimulacaoPartidaView implements ScreenView {
    private final BorderPane root;
    private final GameNavigator navigator;
    private final PartidaTorneio partidaTorneio;
    private final Partida partida;
    private final RelogioPartida relogio;
    private final boolean brasilEhMandante;
    private final List<Jogador> elencoDoJogador = new ArrayList<>();
    private int minutoAtual = 1;
    private Timeline timeline;
    private boolean jogoEmAndamento = false;
    private boolean substituicaoObrigatoriaPendente = false;

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

    private VBox painelTecnico;
    private ComboBox<Jogador> comboSai;
    private ComboBox<Jogador> comboEntra;
    private Button btnConfirmarSub;
    private Button btnTaticaOfensiva;
    private Button btnTaticaEquilibrada;
    private Button btnTaticaRetranca;

    public SimulacaoPartidaView(GameNavigator navigator, PartidaTorneio partidaTorneio, Partida partida) {
        this.navigator = navigator;
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
        
        configurarTimeline();
        atualizarEstadoBotoesTempo();
        atualizarEstadoPainelTecnico();
        renderizarElencoAoVivo();
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
        painelPlacar.getChildren().addAll(lblTempo, timesBox);

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

        btnVelLenta = new Button("⏪ Lento");
        btnVelLenta.setOnAction(e -> alterarVelocidade(0.5));

        btnVelNormal = new Button("▶ Normal");
        btnVelNormal.setOnAction(e -> alterarVelocidade(1.0));

        btnVelRapida = new Button("⏩ Rápido");
        btnVelRapida.setOnAction(e -> alterarVelocidade(3.0));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnVoltar = new Button("Voltar ao Hub");
        btnVoltar.getStyleClass().add("ghost-button");
        btnVoltar.setDisable(true);
        btnVoltar.setOnAction(e -> {
            if (timeline != null) timeline.stop();
            navigator.showHub();
        });

        controles.getChildren().addAll(lblControle, btnPausar, btnVelLenta, btnVelNormal, btnVelRapida, spacer, btnVoltar);
        return controles;
    }

    private void configurarTimeline() {
        KeyFrame frame = new KeyFrame(Duration.millis(800), event -> avancarMinuto()); 
        timeline = new Timeline(frame);
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void alterarVelocidade(double taxa) {
        if (minutoAtual > 90) return;

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
        btnVelLenta.getStyleClass().setAll(timeline.getRate() == 0.5 && jogoEmAndamento ? "primary-button" : "secondary-button");
        btnVelNormal.getStyleClass().setAll(timeline.getRate() == 1.0 && jogoEmAndamento ? "primary-button" : "secondary-button");
        btnVelRapida.getStyleClass().setAll(timeline.getRate() == 3.0 && jogoEmAndamento ? "primary-button" : "secondary-button");
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
        //javaFX nao renderiza direito o emoji de bandeira (vira quadradinho), entao usamos a sigla do pais como selo
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
        } else {
            List<Jogador> batedoresEscolhidos = List.of();

            if (partida.getGolsMandante() == partida.getGolsVisitante()) {
                batedoresEscolhidos = escolherBatedoresPenaltis(gerenciadorTorneio.getBrasil());

                if (batedoresEscolhidos.isEmpty()) {
                    prepararNovaEscolhaDeBatedores();
                    return;
                }
            }

            gerenciadorTorneio.registrarResultadoBrasilMataMata(partidaTorneio.getId(), partida, batedoresEscolhidos);
            gerenciadorTorneio.simularAteProximaPartidaBrasilOuFim();
        }
        
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
    }

    private List<Jogador> escolherBatedoresPenaltis(Time brasil) {
        List<Jogador> disponiveis = new ArrayList<>();
        List<Jogador> escolhidos = new ArrayList<>();

        for (Jogador jogador : brasil.getTitulares()) {
            if ("Ativo".equals(jogador.getStatus())) {
                disponiveis.add(jogador);
            }
        }

        if (disponiveis.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.ERROR, "O Brasil não tem nenhum titular ativo para disputar os pênaltis.");
            alerta.initOwner(root.getScene().getWindow());
            alerta.showAndWait();
            return List.of();
        }

        int quantidadeDeCobrancas = Math.min(5, disponiveis.size()); //se sobrou menos de 5 jogadores em campo, eles revezam as cobrancas

        for (int cobranca = 1; cobranca <= quantidadeDeCobrancas; cobranca++) {
            List<String> nomes = new ArrayList<>();
            for (Jogador jogador : disponiveis) {
                nomes.add(jogador.getNome() + " - " + jogador.getPosicao());
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(nomes.get(0), nomes);
            dialog.initOwner(root.getScene().getWindow());
            dialog.setTitle("Decisão por pênaltis");
            dialog.setHeaderText("Escolha o batedor da " + cobranca + "ª cobrança");
            dialog.setContentText("Jogador:");

            Optional<String> escolha = dialog.showAndWait();
            if (escolha.isEmpty()) {
                return List.of();
            }

            int indiceEscolhido = nomes.indexOf(escolha.get());
            escolhidos.add(disponiveis.remove(indiceEscolhido));
        }

        return List.copyOf(escolhidos);
    }

    private void prepararNovaEscolhaDeBatedores() {
        lblTempo.setText("PÊNALTIS");
        btnPausar.setText("Escolher batedores");
        btnPausar.setDisable(false);
        btnPausar.setOnAction(event -> finalizarPartida());
        btnVelLenta.setDisable(true);
        btnVelNormal.setDisable(true);
        btnVelRapida.setDisable(true);
        painelTecnico.setDisable(true);
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}