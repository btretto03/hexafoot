package hexafoot.ui.view;

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

    public HubView(GameNavigator navigator) {
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        List<Time> selecoesInternacionais = navigator.getSession().getSelecoesInternacionais();

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

        VBox calendarioPanel = criarCalendarioCompacto(LocalDate.now());

        topBar.getChildren().addAll(heroCopy, calendarioPanel);
        HBox.setHgrow(heroCopy, Priority.ALWAYS);

        VBox nextStepsPanel = criarMenuProximosPassos(navigator);
        VBox championshipPanel = criarPainelCampeonato(selecoesInternacionais);

        HBox content = new HBox(18, nextStepsPanel, championshipPanel);
        content.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(championshipPanel, Priority.ALWAYS);

        layout.getChildren().addAll(topBar, content);
        VBox.setVgrow(content, Priority.ALWAYS);

        root.setCenter(layout);
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
        partidas.setOnAction(event -> navigator.showFeaturePlaceholder(
                "Simulação da partida",
                "Esta tela exibirá placar, cronômetro, eventos minuto a minuto e substituições."));

        Button tabela = new Button("Consultar grupos e mata-mata");
        tabela.getStyleClass().add("secondary-button");
        tabela.setOnAction(event -> navigator.showFeaturePlaceholder(
                "Tabelas e chaveamento",
                "Aqui serão exibidas as classificações dos grupos e a árvore do mata-mata."));

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

    private VBox criarPainelCampeonato(List<Time> selecoesInternacionais) {
        Label title = new Label("Tabela do campeonato");
        title.getStyleClass().add("card-title");

        Label subtitle = new Label("Classificação organizada por grupos, com pontos, saldo e desempenho atual de cada seleção.");
        subtitle.getStyleClass().add("card-text");
        subtitle.setWrapText(true);

        VBox groupsContainer = new VBox(14);
        groupsContainer.getStyleClass().add("groups-container");

        Map<String, List<Time>> grupos = agruparPorGrupo(selecoesInternacionais);
        for (Map.Entry<String, List<Time>> entry : grupos.entrySet()) {
            groupsContainer.getChildren().add(criarCartaoGrupo(entry.getKey(), entry.getValue()));
        }

        ScrollPane scrollPane = new ScrollPane(groupsContainer);
        scrollPane.getStyleClass().add("championship-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox panel = new VBox(12, title, subtitle, scrollPane);
        panel.getStyleClass().add("info-card");
        panel.getStyleClass().add("championship-panel");
        panel.setPadding(new Insets(22));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return panel;
    }

    private VBox criarCartaoGrupo(String grupo, List<Time> times) {
        Label label = new Label(grupo);
        label.getStyleClass().add("group-title");

        TableView<Time> tabela = criarTabelaClassificacao(times);
        VBox card = new VBox(10, label, tabela);
        card.getStyleClass().add("group-card");
        card.setPadding(new Insets(16));
        return card;
    }

    private TableView<Time> criarTabelaClassificacao(List<Time> times) {
        TableView<Time> tabela = new TableView<>();
        tabela.getStyleClass().add("group-table");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabela.setItems(javafx.collections.FXCollections.observableArrayList(new ArrayList<>(times)));
        tabela.setPrefHeight(220);
        tabela.setMinHeight(220);
        tabela.setMaxHeight(220);

        tabela.getColumns().add(criarColunaTexto("Time", time -> time.getNome(), 1.6));
        tabela.getColumns().add(criarColunaInteiro("P", Time::getPontos, 0.6));
        tabela.getColumns().add(criarColunaInteiro("J", time -> time.getVitorias() + time.getEmpates() + time.getDerrotas(), 0.6));
        tabela.getColumns().add(criarColunaInteiro("V", Time::getVitorias, 0.6));
        tabela.getColumns().add(criarColunaInteiro("E", Time::getEmpates, 0.6));
        tabela.getColumns().add(criarColunaInteiro("D", Time::getDerrotas, 0.6));
        tabela.getColumns().add(criarColunaInteiro("GP", Time::getGolsMarcados, 0.7));
        tabela.getColumns().add(criarColunaInteiro("GC", Time::getGolsSofridos, 0.7));
        tabela.getColumns().add(criarColunaInteiro("SG", Time::getSaldoGols, 0.7));

        return tabela;
    }

    private TableColumn<Time, String> criarColunaTexto(String titulo, java.util.function.Function<Time, String> extrator, double largura) {
        TableColumn<Time, String> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(celula -> new ReadOnlyStringWrapper(extrator.apply(celula.getValue())));
        coluna.setPrefWidth(largura * 100);
        return coluna;
    }

    private TableColumn<Time, Number> criarColunaInteiro(String titulo, java.util.function.ToIntFunction<Time> extrator, double largura) {
        TableColumn<Time, Number> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(celula -> new ReadOnlyIntegerWrapper(extrator.applyAsInt(celula.getValue())));
        coluna.setPrefWidth(largura * 100);
        return coluna;
    }

    private Map<String, List<Time>> agruparPorGrupo(List<Time> selecoes) {
        Map<String, List<Time>> grupos = new LinkedHashMap<>();
        List<Time> ordenadas = new ArrayList<>(selecoes);
        Comparator<Time> comparadorClassificacao = Comparator
            .comparingInt(Time::getPontos).reversed()
            .thenComparing(Comparator.comparingInt(Time::getSaldoGols).reversed())
            .thenComparing(Comparator.comparingInt(Time::getGolsMarcados).reversed())
            .thenComparing(Time::getNome);

        ordenadas.sort(comparadorClassificacao);

        for (int i = 0; i < ordenadas.size(); i++) {
            String grupo = formatarGrupo(i / 4);
            grupos.computeIfAbsent(grupo, key -> new ArrayList<>()).add(ordenadas.get(i));
        }

        for (List<Time> grupo : grupos.values()) {
            grupo.sort(comparadorClassificacao);
        }

        return grupos;
    }

    private String formatarGrupo(int indice) {
        if (indice < 26) {
            return "Grupo " + (char) ('A' + indice);
        }

        return "Grupo " + (indice + 1);
    }

    private VBox criarCalendarioCompacto(LocalDate hoje) {
        Label titulo = new Label("Calendário");
        titulo.getStyleClass().add("card-title");

        Locale localePtBr = Locale.forLanguageTag("pt-BR");
        Label dataAtual = new Label(hoje.getDayOfWeek().getDisplayName(TextStyle.SHORT, localePtBr) + " • " + hoje.format(FORMATO_DATA));
        dataAtual.getStyleClass().add("card-text");

        HBox dias = new HBox(10);
        dias.getStyleClass().add("calendar-row");

        for (int i = 0; i < 5; i++) {
            LocalDate data = hoje.plusDays(i);
            dias.getChildren().add(criarDiaCalendario(data, i == 0));
        }

        VBox card = new VBox(10, titulo, dataAtual, dias);
        card.getStyleClass().add("calendar-card");
        card.setPadding(new Insets(18));
        return card;
    }

    private VBox criarDiaCalendario(LocalDate data, boolean destaque) {
        Locale localePtBr = Locale.forLanguageTag("pt-BR");
        Label diaSemana = new Label(data.getDayOfWeek().getDisplayName(TextStyle.SHORT, localePtBr).toUpperCase(localePtBr));
        diaSemana.getStyleClass().add("calendar-day-name");

        Label numero = new Label(String.valueOf(data.getDayOfMonth()));
        numero.getStyleClass().add("calendar-day-number");

        VBox tile = new VBox(6, diaSemana, numero);
        tile.setAlignment(Pos.CENTER);
        tile.getStyleClass().add("calendar-tile");
        if (destaque) {
            tile.getStyleClass().add("calendar-tile-today");
        }

        return tile;
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