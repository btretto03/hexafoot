package hexafoot.ui.view;

import hexafoot.model.EventoPartida;
import hexafoot.model.Jogador;
import hexafoot.model.Partida;
import hexafoot.model.Time;
import hexafoot.model.strategy.EstrategiaSimulacao;
import hexafoot.model.strategy.TaticaEquilibrada;
import hexafoot.model.strategy.TaticaOfensiva;
import hexafoot.model.strategy.TaticaRetranca;
import hexafoot.service.simulacao.RelogioPartida;
import hexafoot.ui.GameNavigator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SimulacaoPartidaView implements ScreenView {
    private final BorderPane root;
    private final GameNavigator navigator;
    
    // Core da Simulação
    private final Partida partida;
    private final RelogioPartida relogio;
    private int minutoAtual = 1;
    private Timeline timeline;
    private boolean jogoEmAndamento = false;

    // Componentes Visuais Principais
    private Label lblTempo;
    private Label lblPlacarMandante;
    private Label lblPlacarVisitante;
    private ListView<String> listaEventosVisual;
    private Button btnPausePlay;

    // Componentes do Painel do Técnico (Lateral)
    private VBox painelTecnico;
    private ComboBox<Jogador> comboSai;
    private ComboBox<Jogador> comboEntra;
    private Button btnConfirmarSub;
    private Button btnTaticaOfensiva;
    private Button btnTaticaEquilibrada;
    private Button btnTaticaRetranca;

    public SimulacaoPartidaView(GameNavigator navigator, Partida partida) {
        this.navigator = navigator;
        this.partida = partida;
        
        // Inicializa o motor
        this.relogio = new RelogioPartida();
        this.relogio.adicionarProcessadoresPadrao();
        
        this.root = new BorderPane();
        this.root.getStyleClass().add("screen-root");

        // Layout Centralizado para Placar e Eventos
        VBox centroLayout = new VBox(20);
        centroLayout.getChildren().addAll(criarPlacar(), criarPainelEventos());
        VBox.setVgrow(centroLayout.getChildren().get(1), Priority.ALWAYS);

        // Montagem do BorderPane
        root.setCenter(centroLayout);
        root.setRight(criarPainelTecnicoLateral());
        root.setBottom(criarControlesInferiores());
        
        BorderPane.setMargin(centroLayout, new Insets(24, 12, 12, 24));
        
        configurarTimeline();
        atualizarEstadoPainelTecnico(); // Começa desativado até o "Iniciar" virar "Pausar"
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

        Label lblMandante = new Label(partida.getMandante().getNome());
        lblMandante.getStyleClass().add("display-title");
        lblMandante.setStyle("-fx-font-size: 32px;");
        
        lblPlacarMandante = new Label("0");
        lblPlacarMandante.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblX = new Label("X");
        lblX.setStyle("-fx-font-size: 32px; -fx-text-fill: gray;");

        lblPlacarVisitante = new Label("0");
        lblPlacarVisitante.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblVisitante = new Label(partida.getVisitante().getNome());
        lblVisitante.getStyleClass().add("display-title");
        lblVisitante.setStyle("-fx-font-size: 32px;");

        timesBox.getChildren().addAll(lblMandante, lblPlacarMandante, lblX, lblPlacarVisitante, lblVisitante);
        painelPlacar.getChildren().addAll(lblTempo, timesBox);

        return painelPlacar;
    }

    private VBox criarPainelEventos() {
        VBox painel = new VBox(10);
        painel.getStyleClass().add("info-card");
        painel.setPadding(new Insets(20));

        Label titulo = new Label("Lances da Partida");
        titulo.getStyleClass().add("card-title");

        listaEventosVisual = new ListView<>();
        listaEventosVisual.getStyleClass().add("championship-scroll");
        VBox.setVgrow(listaEventosVisual, Priority.ALWAYS);

        painel.getChildren().addAll(titulo, listaEventosVisual);
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

        // --- SEÇÃO DE VALORES TÁTICOS ---
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

        // --- SEÇÃO DE SUBSTITUIÇÕES ---
        Label lblSubTituloSecao = new Label("Substituições (" + partida.getSubstituicoesMandante() + "/5)");
        lblSubTituloSecao.getStyleClass().add("pitch-row-title");

        comboSai = new ComboBox<>();
        comboSai.setPromptText("Selecione quem sai...");
        comboSai.setMaxWidth(Double.MAX_VALUE);
        configurarFormatacaoCombo(comboSai);

        comboEntra = new ComboBox<>();
        comboEntra.setPromptText("Selecione quem entra...");
        comboEntra.setMaxWidth(Double.MAX_VALUE);
        configurarFormatacaoCombo(comboEntra);

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
        HBox controles = new HBox(15);
        controles.setAlignment(Pos.CENTER);
        controles.setPadding(new Insets(12, 24, 24, 24));

        btnPausePlay = new Button("Iniciar Partida");
        btnPausePlay.getStyleClass().add("primary-button");
        btnPausePlay.setStyle("-fx-min-width: 180px;");
        btnPausePlay.setOnAction(e -> alternarPausa());

        Button btnVoltar = new Button("Voltar ao Hub");
        btnVoltar.getStyleClass().add("ghost-button");
        btnVoltar.setOnAction(e -> {
            if (timeline != null) timeline.stop();
            navigator.showHub();
        });

        controles.getChildren().addAll(btnPausePlay, btnVoltar);
        return controles;
    }

    private void configurarTimeline() {
        KeyFrame frame = new KeyFrame(Duration.millis(400), event -> avancarMinuto());
        timeline = new Timeline(frame);
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void alternarPausa() {
        if (minutoAtual > 90) return;

        if (jogoEmAndamento) {
            timeline.pause();
            btnPausePlay.setText("Continuar Partida");
            btnPausePlay.getStyleClass().setAll("secondary-button");
        } else {
            timeline.play();
            btnPausePlay.setText("Pausar Jogo");
            btnPausePlay.getStyleClass().setAll("primary-button");
        }
        jogoEmAndamento = !jogoEmAndamento;
        atualizarEstadoPainelTecnico();
    }

    private void avancarMinuto() {
        if (minutoAtual > 90) {
            finalizarPartida();
            return;
        }

        int qtdEventosAntes = partida.getEventos().size();
        relogio.processarMinutoIsolado(minutoAtual, partida);

        // Updates de UI Básicos
        lblTempo.setText(minutoAtual + "'");
        lblPlacarMandante.setText(String.valueOf(partida.getGolsMandante()));
        lblPlacarVisitante.setText(String.valueOf(partida.getGolsVisitante()));

        // Injeção de Logs na ListView
        int qtdEventosDepois = partida.getEventos().size();
        if (qtdEventosDepois > qtdEventosAntes) {
            for (int i = qtdEventosAntes; i < qtdEventosDepois; i++) {
                EventoPartida ev = partida.getEventos().get(i);
                listaEventosVisual.getItems().add(0, ev.getMinuto() + "' - " + ev.toString());
            }
        }

        minutoAtual++;
    }

    private void alterarPosturaTatica(String tipo) {
        Time brasil = partida.getMandante();
        if (tipo.equals("ofensiva")) brasil.setTaticaAtual(new TaticaOfensiva());
        else if (tipo.equals("equilibrada")) brasil.setTaticaAtual(new TaticaEquilibrada());
        else if (tipo.equals("retranca")) brasil.setTaticaAtual(new TaticaRetranca());

        atualizarEstiloBotoesTatica();
        listaEventosVisual.getItems().add(0, minutoAtual + "' - 📋 Técnico mudou a postura estratégica da equipe.");
    }

    private void executarSubstituicaoManual() {
        Jogador sai = comboSai.getSelectionModel().getSelectedItem();
        Jogador entra = comboEntra.getSelectionModel().getSelectedItem();

        if (sai == null || entra == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione o jogador que vai sair e o que vai entrar.").showAndWait();
            return;
        }

        // Executa a regra de negócio do seu backend (Partida.java)
        boolean sucesso = partida.substituirMandante(sai, entra);

        if (sucesso) {
            // Força o registro visual do evento na lista de lances
            EventoPartida evSub = new EventoPartida(minutoAtual, "Substituicao", sai, entra);
            partida.adicionarEvento(evSub);
            listaEventosVisual.getItems().add(0, minutoAtual + "' - " + evSub.toString());

            // Recarrega as caixas de opções
            carregarDadosTreinador();
        } else {
            new Alert(Alert.AlertType.ERROR, "Substituição inválida! Verifique o limite de trocas ou a integridade física do atleta.").showAndWait();
        }
    }

    private void atualizarEstadoPainelTecnico() {
        // Bloqueia interações se o jogo estiver rodando ou se já acabou
        boolean desativar = jogoEmAndamento || minutoAtual > 90;
        painelTecnico.setDisable(desativar);

        if (!desativar) {
            carregarDadosTreinador(); // Só gasta processamento populando os combos se o jogo pausar
        }
    }

    private void carregarDadosTreinador() {
        // Popula o ComboBox de quem sai apenas com titulares do Brasil
        comboSai.setItems(FXCollections.observableArrayList(partida.getMandante().getTitulares()));
        
        // Popula o ComboBox de quem entra usando a lógica de reservas disponíveis filtrada do seu backend
        comboEntra.setItems(FXCollections.observableArrayList(partida.getReservasDisponiveisMandante()));
        
        // Atualiza a contagem visual no título da seção
        Label lblSub = (Label) painelTecnico.getChildren().get(4);
        lblSub.setText("Substituções (" + partida.getSubstituicoesMandante() + "/5)");
        
        if (!partida.mandantePodeSubstituir()) {
            btnConfirmarSub.setDisable(true);
            btnConfirmarSub.setText("Limite de Trocas Atingido");
        }
    }

    private void atualizarEstiloBotoesTatica() {
        EstrategiaSimulacao taticaAtual = partida.getMandante().getTaticaAtual();
        
        btnTaticaOfensiva.getStyleClass().setAll("secondary-button");
        btnTaticaEquilibrada.getStyleClass().setAll("secondary-button");
        btnTaticaRetranca.getStyleClass().setAll("secondary-button");

        if (taticaAtual instanceof TaticaOfensiva) btnTaticaOfensiva.getStyleClass().setAll("primary-button");
        else if (taticaAtual instanceof TaticaEquilibrada) btnTaticaEquilibrada.getStyleClass().setAll("primary-button");
        else if (taticaAtual instanceof TaticaRetranca) btnTaticaRetranca.getStyleClass().setAll("primary-button");
    }

    private void configurarFormatacaoCombo(ComboBox<Jogador> combo) {
        // Renderiza o Objeto Jogador de forma amigável no menu do JavaFX: "Nome (Posição) - Energia%"
        combo.setCellFactory(lv -> new ListCell<Jogador>() {
            @Override
            protected void updateItem(Jogador item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.getNome() + " (" + item.getPosicao() + ") - " + item.getFisico() + "%");
                }
            }
        });
        combo.setButtonCell(combo.getCellFactory().call(null));
    }

    private void finalizarPartida() {
        timeline.stop();
        jogoEmAndamento = false;
        partida.aplicarResultadoNaTabela();
        
        lblTempo.setText("FIM");
        btnPausePlay.setText("Partida Encerrada");
        btnPausePlay.setDisable(true);
        painelTecnico.setDisable(true);
        listaEventosVisual.getItems().add(0, "🏁 FIM DE JOGO! " + partida.getGolsMandante() + " x " + partida.getGolsVisitante());
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}