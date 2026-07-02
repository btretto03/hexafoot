package hexafoot.ui.view;

import hexafoot.ui.GameNavigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class FeaturePlaceholderView implements ScreenView {
    private final BorderPane root;

    public FeaturePlaceholderView(GameNavigator navigator, String title, String description) {
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        Label pageTitle = new Label(title);
        pageTitle.getStyleClass().add("page-title");

        Label pageDescription = new Label(description);
        pageDescription.getStyleClass().add("page-subtitle");
        pageDescription.setWrapText(true);
        pageDescription.setMaxWidth(620);

        Button backButton = new Button("Voltar ao hub");
        backButton.getStyleClass().add("primary-button");
        backButton.setOnAction(event -> navigator.showHub());

        VBox content = new VBox(16, pageTitle, pageDescription, backButton);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(40));
        content.getStyleClass().add("hero-panel");

        VBox wrapper = new VBox(content);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(40));

        root.setCenter(wrapper);
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}