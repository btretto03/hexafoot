package hexafoot.ui.view;

import hexafoot.dados.GerenciadorSalvamento;
import hexafoot.service.torneio.GerenciadorTorneio;
import hexafoot.ui.GameNavigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Tela de carregamento de jogo salvo.
 */
public class CarregarJogoView implements ScreenView {
    private final BorderPane root;
    private final GerenciadorSalvamento gerenciadorSalvamento;

    public CarregarJogoView(GameNavigator navigator) {
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");
        this.gerenciadorSalvamento = new GerenciadorSalvamento();

        Label pageTitle = new Label("Carregar jogo");
        pageTitle.getStyleClass().add("page-title");

        Label pageSubtitle = new Label("Retome a campanha de onde parou.");
        pageSubtitle.getStyleClass().add("page-subtitle");

        VBox conteudo = gerenciadorSalvamento.existeSalvamento() ? criarCardSalvamento(navigator) : criarMensagemVazia();

        Button backButton = new Button("Voltar ao menu");
        backButton.getStyleClass().add("ghost-button");
        backButton.setOnAction(event -> navigator.showMainMenu());

        VBox content = new VBox(20, pageTitle, pageSubtitle, conteudo, backButton);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(40));
        content.getStyleClass().add("hero-panel");
        content.setMaxWidth(640);

        VBox wrapper = new VBox(content);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(40));

        root.setCenter(wrapper);
    }

    private VBox criarCardSalvamento(GameNavigator navigator) {
        Label lblInfo = new Label("Save encontrado, salvo em " + gerenciadorSalvamento.obterDataSalvamento() + ".");
        lblInfo.getStyleClass().add("card-text");
        lblInfo.setWrapText(true);

        Label lblErro = new Label("");
        lblErro.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 13px;");
        lblErro.setWrapText(true);
        lblErro.setManaged(false);
        lblErro.setVisible(false);

        Button carregarButton = new Button("Carregar campanha");
        carregarButton.getStyleClass().add("primary-button");
        carregarButton.setOnAction(event -> {
            try {
                GerenciadorTorneio gerenciadorTorneio = gerenciadorSalvamento.carregar();
                navigator.getSession().carregarTorneio(gerenciadorTorneio);
                navigator.showHub();
            } catch (Exception erro) {
                lblErro.setText("Não foi possível carregar o jogo salvo");
                lblErro.setManaged(true);
                lblErro.setVisible(true);
            }
        });

        VBox card = new VBox(12, lblInfo, carregarButton, lblErro);
        card.getStyleClass().add("info-card");
        card.setPadding(new Insets(22));
        return card;
    }

    private VBox criarMensagemVazia() {
        Label lblVazio = new Label("Nenhum jogo salvo encontrado.");
        lblVazio.getStyleClass().add("card-text");
        lblVazio.setWrapText(true);

        VBox card = new VBox(lblVazio);
        card.getStyleClass().add("info-card");
        card.setPadding(new Insets(22));
        return card;
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}