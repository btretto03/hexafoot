package hexafoot.ui.view;

import hexafoot.model.Time;
import hexafoot.ui.GameNavigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class HubView implements ScreenView {
    private final BorderPane root;

    public HubView(GameNavigator navigator) {
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        Time brasil = navigator.getSession().getElencoBrasil();

        Label title = new Label("Hub do Técnico");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Painel central entre rodadas, com acesso rápido às próximas telas do simulador.");
        subtitle.getStyleClass().add("page-subtitle");
        subtitle.setWrapText(true);

        VBox header = new VBox(6, title, subtitle);
        header.getStyleClass().add("page-header");
        header.setPadding(new Insets(24));

        GridPane cards = new GridPane();
        cards.setHgap(16);
        cards.setVgap(16);
        cards.setPadding(new Insets(24));

        cards.add(criarCard("Convocados", String.valueOf(brasil.getTitulares().size() + brasil.getReservas().size())), 0, 0);
        cards.add(criarCard("Titulares", String.valueOf(brasil.getTitulares().size())), 1, 0);
        cards.add(criarCard("Reservas", String.valueOf(brasil.getReservas().size())), 2, 0);
        cards.add(criarCard("Seleções IA", String.valueOf(navigator.getSession().getSelecoesInternacionais().size())), 3, 0);

        VBox actions = new VBox(12);
        actions.setPadding(new Insets(0, 24, 24, 24));

        Button escalacao = new Button("Abrir escalação e tática");
        escalacao.getStyleClass().add("primary-button");
        escalacao.setOnAction(event -> navigator.showFeaturePlaceholder(
                "Escalação e tática",
                "Aqui o técnico montará os 11 titulares, reservas, formação e estratégia de jogo."));

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

        actions.getChildren().addAll(escalacao, partidas, tabela, salvar, menu);

        VBox body = new VBox(18, cards, actions);
        body.setAlignment(Pos.TOP_LEFT);

        root.setTop(header);
        root.setCenter(body);
    }

    private VBox criarCard(String title, String value) {
        Label cardTitle = new Label(title);
        cardTitle.getStyleClass().add("card-title");

        Label cardValue = new Label(value);
        cardValue.getStyleClass().add("metric-value");

        VBox card = new VBox(8, cardTitle, cardValue);
        card.getStyleClass().add("metric-card");
        card.setMinWidth(210);
        card.setPadding(new Insets(20));
        return card;
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}