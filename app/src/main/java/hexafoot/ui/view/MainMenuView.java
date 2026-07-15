package hexafoot.ui.view;

import hexafoot.ui.GameNavigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainMenuView extends TelaBase {
    private final BorderPane root;

    public MainMenuView(GameNavigator navigator) {
        super(navigator);
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        VBox layout = new VBox(22);
        layout.setPadding(new Insets(32));
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(1120);

        VBox hero = new VBox(18);
        hero.getStyleClass().add("hero-panel");
        hero.setPadding(new Insets(38));
        hero.setAlignment(Pos.CENTER_LEFT);

        VBox heroCopy = new VBox(16);

        Label eyebrow = new Label("UNICAMP | Futebol de Seleções");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label("Hexafoot 2026");
        title.getStyleClass().add("display-title");

        Label subtitle = new Label("Monte a Seleção Brasileira, organize a campanha e leve o time da convocação à taça com uma experiência mais viva e esportiva.");
        subtitle.getStyleClass().add("hero-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(620);

        Button newGameButton = new Button("Novo jogo");
        newGameButton.getStyleClass().addAll("primary-button", "wide-button");
        newGameButton.setOnAction(event -> navigator.startNewCampaign());

        Button loadButton = new Button("Carregar jogo");
        loadButton.getStyleClass().addAll("secondary-button", "wide-button");
        loadButton.setOnAction(event -> navigator.showCarregarJogo());

        Button exitButton = new Button("Sair");
        exitButton.getStyleClass().addAll("ghost-button", "wide-button");
        exitButton.setOnAction(event -> navigator.exitGame());

        HBox actions = new HBox(12, newGameButton, loadButton, exitButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(10,
            criarPill("48 seleções"),
                criarPill("Copa 2026"),
                criarPill("Brasil em foco"));

        heroCopy.getChildren().addAll(eyebrow, title, subtitle, actions, stats);
        hero.getChildren().add(heroCopy);

        FlowPane highlights = new FlowPane();
        highlights.setHgap(16);
        highlights.setVgap(16);
        highlights.setAlignment(Pos.CENTER);
        highlights.setPrefWrapLength(1060);
        highlights.getChildren().addAll(
                criarDestaque("Convoque", "Escolha o elenco ideal para a estreia da campanha."),
                criarDestaque("Escale", "Ajuste a formação e defina a postura tática."),
                criarDestaque("Jogue", "Siga para a partida com uma interface mais imersiva."));

        layout.getChildren().addAll(hero, highlights);
        StackPane centeredLayout = new StackPane(layout);
        centeredLayout.setAlignment(Pos.CENTER);
        root.setCenter(centeredLayout);
    }

    private VBox criarDestaque(String title, String text) {
        Label cardTitle = new Label(title);
        cardTitle.getStyleClass().add("card-title");

        Label cardText = new Label(text);
        cardText.getStyleClass().add("card-text");
        cardText.setWrapText(true);

        VBox card = new VBox(8, cardTitle, cardText);
        card.getStyleClass().add("info-card");
        card.setPadding(new Insets(22));
        card.setMinWidth(260);
        card.setPrefWidth(300);
        card.setMaxWidth(300);
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
