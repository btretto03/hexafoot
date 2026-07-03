package hexafoot.ui;

import hexafoot.ui.view.ConvocacaoView;
import hexafoot.ui.view.EscalacaoTaticaView;
import hexafoot.ui.view.FeaturePlaceholderView;
import hexafoot.ui.view.HubView;
import hexafoot.ui.view.MainMenuView;
import hexafoot.ui.view.ScreenView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameNavigator {
    private static final double WIDTH = 1280;
    private static final double HEIGHT = 720;

    private final Stage stage;
    private final GameSession session;

    public GameNavigator(Stage stage, GameSession session) {
        this.stage = stage;
        this.session = session;
    }

    public void showMainMenu() {
        applyScene(new MainMenuView(this));
    }

    public void startNewCampaign() {
        session.iniciarNovoJogo();
        applyScene(new ConvocacaoView(this));
    }

    public void showHub() {
        applyScene(new HubView(this));
    }

    public void showEscalacaoTatica() {
        applyScene(new EscalacaoTaticaView(this));
    }

    public void showFeaturePlaceholder(String title, String description) {
        applyScene(new FeaturePlaceholderView(this, title, description));
    }

    public void exitGame() {
        stage.close();
    }

    public GameSession getSession() {
        return session;
    }

    private void applyScene(ScreenView view) {
        Scene scene = new Scene(view.getRoot(), WIDTH, HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles/hexafoot.css").toExternalForm());
        stage.setScene(scene);
    }
}