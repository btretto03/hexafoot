package hexafoot;

import hexafoot.ui.GameNavigator;
import hexafoot.ui.GameSession;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        GameSession session = new GameSession();
        GameNavigator navigator = new GameNavigator(stage, session);

        stage.setTitle("Hexafoot 2026: caminho para a Copa");
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        navigator.showMainMenu();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
