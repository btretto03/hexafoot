package hexafoot.ui.view;

import hexafoot.model.Grupo;
import hexafoot.model.Time;
import hexafoot.service.torneio.GerenciadorTorneio;
import hexafoot.ui.GameNavigator;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class TabelasChaveamentoView implements ScreenView {
    private final BorderPane root;

    public TabelasChaveamentoView(GameNavigator navigator) {
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        GerenciadorTorneio gerenciadorTorneio = navigator.getSession().getGerenciadorTorneio();

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(24));

        Label title = new Label("Tabelas e Chaveamento");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Classificação organizada por grupos, com pontos, saldo e desempenho atual de cada seleção.");
        subtitle.getStyleClass().add("page-subtitle");
        subtitle.setWrapText(true);

        Button voltar = new Button("Voltar ao hub");
        voltar.getStyleClass().add("primary-button");
        voltar.setOnAction(event -> navigator.showHub());

        VBox header = new VBox(12, title, subtitle, voltar);
        header.getStyleClass().add("hero-panel");
        header.setPadding(new Insets(24));

        VBox groupsContainer = new VBox(14);
        groupsContainer.getStyleClass().add("groups-container");

        for (Grupo grupo : gerenciadorTorneio.getGrupos()) {
            List<Time> classificacao = gerenciadorTorneio.getClassificacaoGrupo(grupo.getIdentificador());
            groupsContainer.getChildren().add(criarCartaoGrupo("Grupo " + grupo.getIdentificador(), classificacao));
        }

        ScrollPane scrollPane = new ScrollPane(groupsContainer);
        scrollPane.getStyleClass().add("championship-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        layout.getChildren().addAll(header, scrollPane);
        VBox.setVgrow(layout, Priority.ALWAYS);

        root.setCenter(layout);
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

    @Override
    public Parent getRoot() {
        return root;
    }
}
