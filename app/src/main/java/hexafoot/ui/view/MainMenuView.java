package hexafoot.ui.view;

import hexafoot.ui.GameNavigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainMenuView implements ScreenView {
    private final BorderPane root;

    public MainMenuView(GameNavigator navigator) {
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        VBox hero = new VBox(18);
        hero.getStyleClass().add("hero-panel");
        hero.setPadding(new Insets(40));
        hero.setAlignment(Pos.CENTER_LEFT);

        Label eyebrow = new Label("UNICAMP | Projeto de Simulação");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label("Hexafoot 2026");
        title.getStyleClass().add("display-title");

        Label subtitle = new Label("Road to the Cup. Gerencie a Seleção Brasileira, convoque, escale e conduza a Copa do Mundo do início ao fim.");
        subtitle.getStyleClass().add("hero-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(540);

        Button newGameButton = new Button("Novo jogo");
        newGameButton.getStyleClass().addAll("primary-button", "wide-button");
        newGameButton.setOnAction(event -> navigator.startNewCampaign());

        Button loadButton = new Button("Carregar jogo");
        loadButton.getStyleClass().addAll("secondary-button", "wide-button");
        loadButton.setOnAction(event -> navigator.showFeaturePlaceholder(
                "Carregar jogo salvo",
                "Nesta etapa a interface exibirá a lista de arquivos de salvamento e restaurará o estado da campanha."));

        Button exitButton = new Button("Sair");
        exitButton.getStyleClass().addAll("ghost-button", "wide-button");
        exitButton.setOnAction(event -> navigator.exitGame());

        HBox actions = new HBox(12, newGameButton, loadButton, exitButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        hero.getChildren().addAll(eyebrow, title, subtitle, actions);

        VBox sideCard = new VBox(14);
        sideCard.getStyleClass().add("info-card");
        sideCard.setPadding(new Insets(28));
        sideCard.setMaxWidth(340);

        Label cardTitle = new Label("Fluxo inicial");
        cardTitle.getStyleClass().add("card-title");

        Label cardText = new Label("1. Convocação do Brasil\n2. Hub do técnico\n3. Escalação e tática\n4. Partida, pós-jogo e classificação");
        cardText.getStyleClass().add("card-text");

        sideCard.getChildren().addAll(cardTitle, cardText);

        StackPane center = new StackPane(hero);
        center.setPadding(new Insets(36));

        root.setCenter(center);
        root.setRight(sideCard);
        BorderPane.setAlignment(sideCard, Pos.CENTER);
        BorderPane.setMargin(sideCard, new Insets(36, 36, 36, 0));
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}