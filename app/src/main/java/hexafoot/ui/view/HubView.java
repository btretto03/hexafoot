package hexafoot.ui.view;

import hexafoot.dados.GerenciadorSalvamento;
import hexafoot.model.Formacao;
import hexafoot.model.Jogador;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.Time;
import hexafoot.model.strategy.EstrategiaSimulacao;
import hexafoot.model.strategy.TaticaOfensiva;
import hexafoot.model.strategy.TaticaRetranca;
import hexafoot.service.torneio.GerenciadorTorneio;
import hexafoot.ui.GameNavigator;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Exibe o estado da campanha ativa e conduz o usuário à próxima partida ou aos ajustes do elenco.
 */
public class HubView extends TelaBase {
    private final BorderPane root;

    public HubView(GameNavigator navigator) {
        super(navigator);
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(24));

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.TOP_LEFT);

        VBox heroCopy = new VBox(14);

        Label title = new Label("Central do Técnico");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Painel central entre rodadas, com acesso rápido à convocação, à escalação e às próximas partidas.");
        subtitle.getStyleClass().add("page-subtitle");
        subtitle.setWrapText(true);

        HBox status = new HBox(10,
            criarPill("Brasil pronto"),
            criarPill("Elenco vivo"),
            criarPill("Próxima rodada"));

        heroCopy.getChildren().addAll(title, subtitle, status);

        topBar.getChildren().addAll(heroCopy);
        HBox.setHgrow(heroCopy, Priority.ALWAYS);

        VBox nextStepsPanel = criarMenuProximosPassos(navigator);
        VBox elencoPanel = criarPainelElenco(navigator);

        HBox content = new HBox(18, nextStepsPanel, elencoPanel);
        content.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(elencoPanel, Priority.ALWAYS);

        layout.getChildren().add(topBar);

        GerenciadorTorneio gerenciadorTorneio = navigator.getSession().getGerenciadorTorneio();
        if (gerenciadorTorneio.campanhaBrasilEncerrada()) {
            layout.getChildren().add(criarBannerResultado(gerenciadorTorneio.getResultadoFinalBrasil()));
        }

        layout.getChildren().add(content);
        VBox.setVgrow(content, Priority.ALWAYS);

        root.setCenter(layout);
    }

    /**
     * Traduz o código final da campanha em um aviso persistente no hub.
     *
     * @param resultado código de colocação; valores não premiados são tratados como eliminação
     * @return banner correspondente ao resultado
     */
    private VBox criarBannerResultado(String resultado) {
        String texto;
        String corFundo;
        String corBorda;

        switch (resultado) {
            case "CAMPEAO":
                texto = "🏆 CAMPEÃO DO MUNDO! O Brasil conquistou a Copa de 2026!";
                corFundo = "rgba(240, 213, 138, 0.15)";
                corBorda = "rgba(240, 213, 138, 0.4)";
                break;
            case "VICE_CAMPEAO":
                texto = "🥈 Vice-campeão! O Brasil perdeu a final, mas fez uma grande campanha.";
                corFundo = "rgba(139, 240, 161, 0.1)";
                corBorda = "rgba(139, 240, 161, 0.3)";
                break;
            case "TERCEIRO_LUGAR":
                texto = "🥉 Terceiro lugar! O Brasil fecha a Copa no pódio.";
                corFundo = "rgba(139, 240, 161, 0.1)";
                corBorda = "rgba(139, 240, 161, 0.3)";
                break;
            case "QUARTO_LUGAR":
                texto = "4º lugar. O Brasil não conseguiu o pódio dessa vez.";
                corFundo = "rgba(240, 213, 138, 0.1)";
                corBorda = "rgba(240, 213, 138, 0.3)";
                break;
            default:
                texto = "❌ O Brasil foi eliminado da Copa de 2026. Confira o chaveamento completo em \"Consultar grupos e mata-mata\".";
                corFundo = "rgba(255, 107, 107, 0.1)";
                corBorda = "rgba(255, 107, 107, 0.3)";
                break;
        }

        Label lblResultado = new Label(texto);
        lblResultado.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblResultado.setWrapText(true);

        VBox banner = new VBox(lblResultado);
        banner.setPadding(new Insets(18));
        banner.setStyle("-fx-background-color: " + corFundo + "; -fx-background-radius: 14; -fx-border-color: " + corBorda + "; -fx-border-radius: 14; -fx-border-width: 1;");
        return banner;
    }

    private VBox criarMenuProximosPassos(GameNavigator navigator) {
        VBox actions = new VBox(12);
        actions.getStyleClass().add("next-steps-stack");

        Label sectionTitle = new Label("Próximos passos");
        sectionTitle.getStyleClass().add("card-title");

        Label sectionText = new Label("Atalhos para seguir a experiência sem perder a sensação de jogo.");
        sectionText.getStyleClass().add("card-text");
        sectionText.setWrapText(true);

        Button escalacao = new Button("Abrir escalação e tática");
        escalacao.getStyleClass().add("primary-button");
        escalacao.setOnAction(event -> navigator.showEscalacaoTatica());

        PartidaTorneio proximaPartidaBrasil = navigator.getSession().getGerenciadorTorneio().getProximaPartidaBrasil().orElse(null);

        Button partidas = new Button("Jogar próxima partida");
        partidas.getStyleClass().add("secondary-button");
        partidas.setDisable(proximaPartidaBrasil == null);
        partidas.setOnAction(event -> navigator.showSimulacaoPartida(proximaPartidaBrasil));

        Button tabela = new Button("Consultar grupos e mata-mata");
        tabela.getStyleClass().add("secondary-button");
        tabela.setOnAction(event -> navigator.showTabelasChaveamento());

        Button salvar = new Button("Salvar progresso");
        salvar.getStyleClass().add("secondary-button");
        salvar.setOnAction(event -> salvarProgresso(salvar, navigator));

        Button menu = new Button("Voltar ao menu");
        menu.getStyleClass().add("ghost-button");
        menu.setOnAction(event -> navigator.showMainMenu());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        actions.getChildren().addAll(sectionTitle, sectionText, escalacao, partidas, tabela, salvar, spacer, menu);

        VBox panel = new VBox(14, actions);
        panel.getStyleClass().add("info-card");
        panel.getStyleClass().add("next-steps-panel");
        panel.setPadding(new Insets(22));
        panel.setPrefWidth(320);
        panel.setMaxWidth(340);
        panel.setMinHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(panel, Priority.ALWAYS);
        return panel;
    }

    /**
     * Sobrescreve o slot único de salvamento e mostra o resultado temporariamente no botão.
     *
     * @param botao botão usado para o retorno visual da operação
     * @param navigator navegador cuja sessão contém o torneio a salvar
     */
    private void salvarProgresso(Button botao, GameNavigator navigator) {
        GerenciadorSalvamento gerenciadorSalvamento = new GerenciadorSalvamento();
        String textoOriginal = botao.getText();

        try {
            gerenciadorSalvamento.salvar(navigator.getSession().getGerenciadorTorneio());
            botao.setText("Progresso salvo!");
        } catch (IOException erro) {
            botao.setText("Erro ao salvar");
        }

        PauseTransition pausa = new PauseTransition(Duration.seconds(2));
        pausa.setOnFinished(event -> botao.setText(textoOriginal));
        pausa.play();
    }

    private VBox criarPainelElenco(GameNavigator navigator) {
        Time brasil = navigator.getSession().getElencoBrasil();

        Label title = new Label("[BRA] Elenco da Seleção");
        title.getStyleClass().add("card-title");

        Label subtitle = new Label("Formação " + formatarFormacao(brasil.getFormacaoAtual()) + " · Postura " + formatarTatica(brasil.getTaticaAtual())
                + ". Pra trocar a formação e a escalação, use \"Abrir escalação e tática\".");
        subtitle.getStyleClass().add("card-text");
        subtitle.setWrapText(true);

        VBox listaJogadores = new VBox(8);
        for (Jogador jogador : brasil.getTitulares()) {
            listaJogadores.getChildren().add(criarLinhaJogador(jogador, "TITULAR"));
        }
        for (Jogador jogador : brasil.getReservas()) {
            listaJogadores.getChildren().add(criarLinhaJogador(jogador, "BANCO"));
        }

        ScrollPane scroll = new ScrollPane(listaJogadores);
        scroll.getStyleClass().add("championship-scroll");
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        VBox panel = new VBox(12, title, subtitle, scroll);
        panel.getStyleClass().add("info-card");
        panel.getStyleClass().add("championship-panel");
        panel.setPadding(new Insets(22));
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return panel;
    }

    /**
     * Representa um atleta com sua condição atual no elenco e seu desgaste.
     *
     * @param jogador atleta exibido
     * @param situacaoCampo rótulo de vínculo, como {@code TITULAR} ou {@code BANCO}
     * @return linha visual do atleta
     */
    private HBox criarLinhaJogador(Jogador jogador, String situacaoCampo) {
        Label lblSituacao = new Label(situacaoCampo);
        lblSituacao.getStyleClass().add("status-pill");
        lblSituacao.setStyle("-fx-min-width: 70; -fx-alignment: center;");

        Label lblNome = new Label(jogador.getNome());
        lblNome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fbff;");

        Label lblPosicao = new Label(jogador.getPosicao());
        lblPosicao.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255, 255, 255, 0.6);");

        Label lblStatus = new Label(jogador.getStatus());
        lblStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255, 255, 255, 0.6);");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblFisico = new Label(jogador.getFisico() + "%");
        lblFisico.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;" + corDoFisico(jogador.getFisico()));

        HBox card = new HBox(15, lblSituacao, lblNome, lblPosicao, spacer, lblStatus, lblFisico);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 12; -fx-padding: 10 18 10 18;");
        return card;
    }

    private String corDoFisico(int fisico) {
        if (fisico >= 70) {
            return " -fx-text-fill: #8bf0a1;";
        }
        if (fisico >= 40) {
            return " -fx-text-fill: #f0d58b;";
        }
        return " -fx-text-fill: #ff6b6b;";
    }

    private String formatarFormacao(Formacao formacao) {
        switch (formacao) {
            case F_4_3_3:
                return "4-3-3";
            case F_3_4_3:
                return "3-4-3";
            case F_4_2_4:
                return "4-2-4";
            case F_4_4_2:
                return "4-4-2";
            case F_4_2_3_1:
                return "4-2-3-1";
            case F_3_5_2:
                return "3-5-2";
            case F_5_4_1:
                return "5-4-1";
            case F_5_3_2:
                return "5-3-2";
            case F_4_5_1:
                return "4-5-1";
            default:
                return formacao.name();
        }
    }

    private String formatarTatica(EstrategiaSimulacao tatica) {
        if (tatica instanceof TaticaOfensiva) {
            return "Pressão";
        }
        if (tatica instanceof TaticaRetranca) {
            return "Retranca";
        }
        return "Posse de bola";
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
