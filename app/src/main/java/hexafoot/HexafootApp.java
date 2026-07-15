package hexafoot;

import hexafoot.ui.GameNavigator;
import hexafoot.ui.GameSession;
import javafx.application.Application;
import javafx.stage.Stage;

/**
*Classe responsável por iniciar a aplicação, criar a sessão de jogo e o navegador de telas.
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