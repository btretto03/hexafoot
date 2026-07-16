package hexafoot;

import hexafoot.ui.GameNavigator;
import hexafoot.ui.GameSession;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Ponto de entrada principal da aplicação JavaFX.
 */
public class HexafootApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Hexafoot 2026: caminho para a Copa");

        GameSession sessao = new GameSession();
        GameNavigator navigator = new GameNavigator(stage, sessao);
        navigator.showMainMenu();

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
