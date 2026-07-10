package hexafoot.ui.view;

import hexafoot.model.Partida;
import hexafoot.model.Time;
import hexafoot.ui.GameNavigator;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HubView implements ScreenView {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM");
    private final BorderPane root;

    private final VBox calendarContainer = new VBox(14);
    private LocalDate dataSelecionada = LocalDate.of(2026, 6, 11);
    private LocalDate dataFocoCalendario = LocalDate.of(2026, 6, 11);
    private final Map<LocalDate, List<MapPartida>> partidasPorData = new java.util.HashMap<>();

    public HubView(GameNavigator navigator) {
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        carregarPartidasCalendario();

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(24));

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.TOP_LEFT);

        VBox heroCopy = new VBox(14);

        Label title = new Label("Central do Técnico");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Painel central entre rodadas, com acesso rápido à convocação, à escalação e às próximas partidas.");
        subtitle.getStyleClass().add("page-subtitle");
        subtitle.setWrapText(true);

        HBox status = new HBox(10,
            criarPill("Brasil pronto"),
            criarPill("Elenco vivo"),
            criarPill("Próxima rodada"));

        heroCopy.getChildren().addAll(title, subtitle, status);

        topBar.getChildren().addAll(heroCopy);
        HBox.setHgrow(heroCopy, Priority.ALWAYS);

        VBox nextStepsPanel = criarMenuProximosPassos(navigator);
        VBox calendarPanel = criarPainelCalendario();

        HBox content = new HBox(18, nextStepsPanel, calendarPanel);
        content.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(calendarPanel, Priority.ALWAYS);

        layout.getChildren().addAll(topBar, content);
        VBox.setVgrow(content, Priority.ALWAYS);

        root.setCenter(layout);

        // Inicializar seleção no primeiro dia da Copa
        selecionarData(LocalDate.of(2026, 6, 11));
    }

    private VBox criarMenuProximosPassos(GameNavigator navigator) {
        VBox actions = new VBox(12);
        actions.getStyleClass().add("next-steps-stack");

        Label sectionTitle = new Label("Próximos passos");
        sectionTitle.getStyleClass().add("card-title");

        Label sectionText = new Label("Atalhos para seguir a experiência sem perder a sensação de jogo.");
        sectionText.getStyleClass().add("card-text");
        sectionText.setWrapText(true);

        Button escalacao = new Button("Abrir escalação e tática");
        escalacao.getStyleClass().add("primary-button");
        escalacao.setOnAction(event -> navigator.showEscalacaoTatica());

        Button partidas = new Button("Ver partida e simulação");
        partidas.getStyleClass().add("secondary-button");
        partidas.setOnAction(event -> {
            // Pega o elenco do Brasil (já escalado)
            Time brasil = navigator.getSession().getElencoBrasil();
            
            // TODO: Aqui depois você vai puxar o adversário correto baseado no calendário
            // Por enquanto, pegamos a primeira seleção internacional como exemplo de adversário
            Time adversario = navigator.getSession().getSelecoesInternacionais().get(0);
            
            Partida novaPartida = new Partida(brasil, adversario);
            
            // Navega para a tela de simulação
            navigator.showSimulacaoPartida(novaPartida);
        });

        Button tabela = new Button("Consultar grupos e mata-mata");
        tabela.getStyleClass().add("secondary-button");
        tabela.setOnAction(event -> navigator.showTabelasChaveamento());

        Button salvar = new Button("Salvar progresso");
        salvar.getStyleClass().add("secondary-button");
        salvar.setOnAction(event -> navigator.showFeaturePlaceholder(
                "Salvar jogo",
                "A persistência visual será ligada ao repositório de saves e ao serializador do estado do jogo."));

        Button menu = new Button("Voltar ao menu");
        menu.getStyleClass().add("ghost-button");
        menu.setOnAction(event -> navigator.showMainMenu());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        actions.getChildren().addAll(sectionTitle, sectionText, escalacao, partidas, tabela, salvar, spacer, menu);

        VBox panel = new VBox(14, actions);
        panel.getStyleClass().add("info-card");
        panel.getStyleClass().add("next-steps-panel");
        panel.setPadding(new Insets(22));
        panel.setPrefWidth(320);
        panel.setMaxWidth(340);
        panel.setMinHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private static class MapPartida {
        final String rodada;
        final String grupo;
        final String mandante;
        final String visitante;

        MapPartida(String rodada, String grupo, String mandante, String visitante) {
            this.rodada = rodada;
            this.grupo = grupo;
            this.mandante = mandante;
            this.visitante = visitante;
        }
    }

    private void carregarPartidasCalendario() {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(
                getClass().getResourceAsStream("/data/info_torneio/calendario_fase_grupos.csv"), java.nio.charset.StandardCharsets.UTF_8))) {
            
            String linha;
            br.readLine(); // pular cabeçalho
            int matchIndex = 0;
            
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");
                if (campos.length >= 4) {
                    String rodada = campos[0];
                    String grupo = campos[1];
                    String mandante = campos[2];
                    String visitante = campos[3];
                    
                    int diaOffset = matchIndex / 4;
                    LocalDate dataPartida = LocalDate.of(2026, 6, 11).plusDays(diaOffset);
                    
                    MapPartida p = new MapPartida(rodada, grupo, mandante, visitante);
                    partidasPorData.computeIfAbsent(dataPartida, k -> new ArrayList<>()).add(p);
                    matchIndex++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox criarPainelCalendario() {
        Label title = new Label("Calendário da Copa");
        title.getStyleClass().add("card-title");

        Label subtitle = new Label("Acompanhe o cronograma de partidas, rodadas e fases eliminatórias de 11 de junho a 19 de julho de 2026.");
        subtitle.getStyleClass().add("card-text");
        subtitle.setWrapText(true);

        VBox panel = new VBox(12, title, subtitle, calendarContainer);
        panel.getStyleClass().add("info-card");
        panel.getStyleClass().add("championship-panel");
        panel.setPadding(new Insets(22));
        VBox.setVgrow(calendarContainer, Priority.ALWAYS);
        return panel;
    }

    private void selecionarData(LocalDate data) {
        if (data.isBefore(LocalDate.of(2026, 6, 11)) || data.isAfter(LocalDate.of(2026, 7, 19))) {
            return;
        }
        this.dataSelecionada = data;
        
        if (dataSelecionada.isBefore(dataFocoCalendario)) {
            dataFocoCalendario = dataSelecionada;
        } else if (dataSelecionada.isAfter(dataFocoCalendario.plusDays(6))) {
            dataFocoCalendario = dataSelecionada.minusDays(6);
        }
        
        atualizarCalendario();
    }

    private void atualizarCalendario() {
        calendarContainer.getChildren().clear();

        // 1. Barra de Navegação de Data
        HBox navBar = new HBox(20);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(10, 0, 10, 0));

        Button btnLeft = new Button("◀");
        btnLeft.getStyleClass().add("secondary-button");
        btnLeft.setDisable(dataSelecionada.equals(LocalDate.of(2026, 6, 11)));
        btnLeft.setOnAction(event -> selecionarData(dataSelecionada.minusDays(1)));

        Locale localePtBr = Locale.forLanguageTag("pt-BR");
        String textoDia = dataSelecionada.getDayOfWeek().getDisplayName(TextStyle.FULL, localePtBr) + ", " 
                + dataSelecionada.getDayOfMonth() + " de " 
                + dataSelecionada.getMonth().getDisplayName(TextStyle.FULL, localePtBr) + " de " 
                + dataSelecionada.getYear();
        Label lblData = new Label(textoDia.substring(0, 1).toUpperCase(localePtBr) + textoDia.substring(1));
        lblData.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #8bf0a1;");
        lblData.setAlignment(Pos.CENTER);

        Button btnRight = new Button("▶");
        btnRight.getStyleClass().add("secondary-button");
        btnRight.setDisable(dataSelecionada.equals(LocalDate.of(2026, 7, 19)));
        btnRight.setOnAction(event -> selecionarData(dataSelecionada.plusDays(1)));

        Region spacerL = new Region();
        Region spacerR = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);
        HBox.setHgrow(spacerR, Priority.ALWAYS);

        navBar.getChildren().addAll(btnLeft, spacerL, lblData, spacerR, btnRight);

        // 2. Timeline Horizontal
        HBox timeline = criarLinhaDiasCalendario();

        // 3. Área de Partidas
        VBox partidasBox = new VBox(10);
        partidasBox.setPadding(new Insets(15, 0, 0, 0));

        Label lblPartidasTitulo = new Label("Partidas Agendadas");
        lblPartidasTitulo.getStyleClass().add("pitch-row-title");
        lblPartidasTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        partidasBox.getChildren().add(lblPartidasTitulo);

        List<MapPartida> partidasDoDia = partidasPorData.get(dataSelecionada);
        if (partidasDoDia != null && !partidasDoDia.isEmpty()) {
            for (MapPartida p : partidasDoDia) {
                partidasBox.getChildren().add(criarCardPartida(p));
            }
        } else {
            partidasBox.getChildren().add(criarCardMataMata(dataSelecionada));
        }

        ScrollPane partidasScroll = new ScrollPane(partidasBox);
        partidasScroll.getStyleClass().add("pitch-scroll");
        partidasScroll.setFitToWidth(true);
        partidasScroll.setFitToHeight(true);
        VBox.setVgrow(partidasScroll, Priority.ALWAYS);

        calendarContainer.getChildren().addAll(navBar, timeline, partidasScroll);
    }

    private HBox criarLinhaDiasCalendario() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        
        for (int i = 0; i < 7; i++) {
            LocalDate data = dataFocoCalendario.plusDays(i);
            boolean isSelected = data.equals(dataSelecionada);
            
            VBox tile = criarDiaCalendarioGrande(data, isSelected);
            row.getChildren().add(tile);
            HBox.setHgrow(tile, Priority.ALWAYS);
        }
        
        return row;
    }

    private VBox criarDiaCalendarioGrande(LocalDate data, boolean destaque) {
        Locale localePtBr = Locale.forLanguageTag("pt-BR");
        Label diaSemana = new Label(data.getDayOfWeek().getDisplayName(TextStyle.SHORT, localePtBr).toUpperCase(localePtBr));
        diaSemana.getStyleClass().add("calendar-day-name");
        diaSemana.setStyle("-fx-font-size: 11px;");

        Label numero = new Label(String.valueOf(data.getDayOfMonth()));
        numero.getStyleClass().add("calendar-day-number");
        numero.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label mes = new Label(data.getMonth().getDisplayName(TextStyle.SHORT, localePtBr).toUpperCase(localePtBr));
        mes.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(255, 255, 255, 0.5);");

        VBox tile = new VBox(4, diaSemana, numero, mes);
        tile.setAlignment(Pos.CENTER);
        tile.getStyleClass().add("calendar-tile");
        tile.setStyle("-fx-padding: 8; -fx-min-width: 60; -fx-min-height: 70; -fx-cursor: hand;");
        
        if (destaque) {
            tile.getStyleClass().add("calendar-tile-today");
        }
        
        tile.setOnMouseClicked(event -> selecionarData(data));
        return tile;
    }

    private HBox criarCardPartida(MapPartida p) {
        Label lblGrupo = new Label("Grupo " + p.grupo);
        lblGrupo.getStyleClass().add("status-pill");
        lblGrupo.setStyle("-fx-min-width: 70; -fx-alignment: center;");

        Label lblRodada = new Label("Rodada " + p.rodada);
        lblRodada.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255, 255, 255, 0.6);");

        Label lblTimes = new Label(p.mandante + " vs " + p.visitante);
        lblTimes.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fbff;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox card = new HBox(15, lblGrupo, lblTimes, spacer, lblRodada);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 12; -fx-padding: 12 18 12 18;");
        return card;
    }

    private HBox criarCardMataMata(LocalDate data) {
        Label lblFase = new Label();
        lblFase.getStyleClass().add("status-pill");
        lblFase.setStyle("-fx-min-width: 100; -fx-alignment: center; -fx-background-color: rgba(139, 240, 161, 0.2); -fx-text-fill: #8bf0a1; -fx-border-color: rgba(139, 240, 161, 0.3);");

        Label lblTimes = new Label();
        lblTimes.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fbff;");

        if (data.isEqual(LocalDate.of(2026, 6, 29)) || data.isEqual(LocalDate.of(2026, 6, 30)) ||
            data.isEqual(LocalDate.of(2026, 7, 1)) || data.isEqual(LocalDate.of(2026, 7, 2))) {
            lblFase.setText("OITAVAS");
            lblTimes.setText("Confronto Eliminatório - Oitavas de Final");
        } else if (data.isEqual(LocalDate.of(2026, 7, 4)) || data.isEqual(LocalDate.of(2026, 7, 5))) {
            lblFase.setText("QUARTAS");
            lblTimes.setText("Confronto Eliminatório - Quartas de Final");
        } else if (data.isEqual(LocalDate.of(2026, 7, 8)) || data.isEqual(LocalDate.of(2026, 7, 9))) {
            lblFase.setText("SEMIFINAL");
            lblTimes.setText("Confronto Eliminatório - Semifinal");
        } else if (data.isEqual(LocalDate.of(2026, 7, 18))) {
            lblFase.setText("3º LUGAR");
            lblTimes.setText("Disputa de Terceiro Lugar");
        } else if (data.isEqual(LocalDate.of(2026, 7, 19))) {
            lblFase.setText("FINAL");
            lblTimes.setText("Grande Final da Copa do Mundo 2026");
        } else {
            lblFase.setText("FOLGA");
            lblTimes.setText("Dia de Treinamento / Descanso de Atletas");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox card = new HBox(15, lblFase, lblTimes, spacer);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 12; -fx-padding: 12 18 12 18;");
        return card;
    }

    private Label criarPill(String text) {
        Label pill = new Label(text);
        pill.getStyleClass().add("status-pill");
        return pill;
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}