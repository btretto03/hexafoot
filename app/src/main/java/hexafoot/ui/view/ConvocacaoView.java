package hexafoot.ui.view;

import hexafoot.model.Jogador;
import hexafoot.service.simulacao.GerenciadorConvocacao;
import hexafoot.ui.GameNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ConvocacaoView implements ScreenView {
    private final GameNavigator navigator;
    private final BorderPane root;
    private final ObservableList<Jogador> disponiveis;
    private final ObservableList<Jogador> convocados;
    private final TableView<Jogador> tabelaDisponiveis;
    private final TableView<Jogador> tabelaConvocados;
    private final Label contadorLabel;
    private final ProgressBar progresso;
    private final Button avancarButton;

    public ConvocacaoView(GameNavigator navigator) {
        this.navigator = navigator;
        this.root = new BorderPane();
        this.root.getStyleClass().add("screen-root");

        GerenciadorConvocacao gerenciador = navigator.getSession().getGerenciadorConvocacao();
        this.disponiveis = FXCollections.observableArrayList(gerenciador.getJogadoresDisponiveis());
        this.convocados = FXCollections.observableArrayList(gerenciador.getElencoOficial().getTitulares());
        this.convocados.addAll(gerenciador.getElencoOficial().getReservas());

        this.tabelaDisponiveis = criarTabela("Jogadores disponíveis");
        this.tabelaConvocados = criarTabela("Convocados do Brasil");

        tabelaDisponiveis.setItems(disponiveis);
        tabelaConvocados.setItems(convocados);

        this.contadorLabel = new Label();
        this.contadorLabel.getStyleClass().add("status-pill");

        this.progresso = new ProgressBar(0);
        this.progresso.getStyleClass().add("wide-progress");
        this.progresso.setMaxWidth(Double.MAX_VALUE);

        this.avancarButton = new Button("Avançar para o hub");
        this.avancarButton.getStyleClass().add("primary-button");
        this.avancarButton.setOnAction(event -> {
            if (convocados.size() == 26) {
                navigator.showHub();
                return;
            }

            new Alert(Alert.AlertType.WARNING, "A convocação precisa ter exatamente 26 jogadores.").showAndWait();
        });

        Button adicionarButton = new Button("Adicionar >>");
        adicionarButton.getStyleClass().add("secondary-button");
        adicionarButton.setOnAction(event -> adicionarConvocado());

        Button removerButton = new Button("<< Remover");
        removerButton.getStyleClass().add("secondary-button");
        removerButton.setOnAction(event -> removerConvocado());

        Button voltarButton = new Button("Voltar");
        voltarButton.getStyleClass().add("ghost-button");
        voltarButton.setOnAction(event -> navigator.showMainMenu());

        VBox leftPanel = criarPainelComTabela("Base de 50 atletas", tabelaDisponiveis);
        VBox rightPanel = criarPainelComTabela("Convocação final", tabelaConvocados);

        VBox controlPanel = new VBox(14, adicionarButton, removerButton, voltarButton);
        controlPanel.getStyleClass().add("control-panel");
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setPadding(new Insets(24));

        HBox content = new HBox(18, leftPanel, controlPanel, rightPanel);
        content.setPadding(new Insets(24));
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        VBox footer = new VBox(10, contadorLabel, progresso, avancarButton);
        footer.getStyleClass().add("footer-panel");
        footer.setPadding(new Insets(24));

        Label title = new Label("Convocação da Seleção Brasileira");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Selecione 26 jogadores. Os 11 primeiros entram como titulares e os demais formam o banco.");
        subtitle.getStyleClass().add("page-subtitle");
        subtitle.setWrapText(true);

        VBox header = new VBox(6, title, subtitle);
        header.getStyleClass().add("page-header");
        header.setPadding(new Insets(24, 24, 0, 24));

        VBox center = new VBox(16, header, content, footer);
        root.setCenter(center);

        atualizarEstadoVisual();
    }

    private VBox criarPainelComTabela(String titulo, TableView<Jogador> tabela) {
        Label label = new Label(titulo);
        label.getStyleClass().add("card-title");

        VBox panel = new VBox(12, label, tabela);
        panel.getStyleClass().add("table-card");
        panel.setPadding(new Insets(18));
        VBox.setVgrow(tabela, Priority.ALWAYS);
        return panel;
    }

    private TableView<Jogador> criarTabela(String placeholder) {
        TableView<Jogador> tabela = new TableView<>();
        tabela.setPlaceholder(new Label(placeholder));
        tabela.getStyleClass().add("player-table");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        tabela.getColumns().add(criarColuna("Nome", "nome", 180));
        tabela.getColumns().add(criarColuna("Pos.", "posicao", 110));
        tabela.getColumns().add(criarColuna("Ataque", "ataque", 75));
        tabela.getColumns().add(criarColuna("Defesa", "defesa", 75));
        tabela.getColumns().add(criarColuna("Físico", "fisico", 75));
        tabela.getColumns().add(criarColuna("Estresse", "estresse", 80));
        tabela.getColumns().add(criarColuna("Status", "status", 95));

        return tabela;
    }

    private TableColumn<Jogador, ?> criarColuna(String titulo, String propriedade, double largura) {
        TableColumn<Jogador, Object> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(new PropertyValueFactory<>(propriedade));
        coluna.setPrefWidth(largura);
        return coluna;
    }

    private void adicionarConvocado() {
        Jogador selecionado = tabelaDisponiveis.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            return;
        }

        navigator.getSession().getGerenciadorConvocacao().inserirNoElenco(selecionado);
        sincronizarListas();
    }

    private void removerConvocado() {
        Jogador selecionado = tabelaConvocados.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            return;
        }

        navigator.getSession().getGerenciadorConvocacao().removerDoElenco(selecionado);
        sincronizarListas();
    }

    private void sincronizarListas() {
        GerenciadorConvocacao gerenciador = navigator.getSession().getGerenciadorConvocacao();
        disponiveis.setAll(gerenciador.getJogadoresDisponiveis());
        convocados.setAll(gerenciador.getElencoOficial().getTitulares());
        convocados.addAll(gerenciador.getElencoOficial().getReservas());
        atualizarEstadoVisual();
    }

    private void atualizarEstadoVisual() {
        int total = convocados.size();
        contadorLabel.setText(total + "/26 convocados");
        progresso.setProgress(total / 26.0);
        avancarButton.setDisable(total != 26);
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}