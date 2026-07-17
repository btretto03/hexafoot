package hexafoot.ui.view;

import hexafoot.model.FaseTorneio;
import hexafoot.model.Time;
import hexafoot.service.torneio.GerenciadorTorneio;
import hexafoot.ui.BandeiraProvider;
import hexafoot.ui.GameNavigator;
import hexafoot.ui.TocadorDeSons;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Tela de destaque exibida assim que a campanha do Brasil na Copa se encerra,
 * seja pela conquista do título ou por uma eliminação.
 */
public class ResultadoCampanhaView extends TelaBase {
    private final BorderPane root;
    private final VBox cartao;
    private final Label lblTitulo;
    private final Label lblSubtitulo;
    private final Button btnSimularResto;
    private final Button btnChaveamento;
    private final Button btnHub;

    public ResultadoCampanhaView(GameNavigator navigator, String resultado) {
        super(navigator);
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        ImageView bandeira = BandeiraProvider.criarImageViewBandeira("brasil", 96, 72);

        lblTitulo = new Label(obterTitulo(resultado));
        lblTitulo.getStyleClass().add("display-title");
        lblTitulo.setStyle("-fx-font-size: 44px; -fx-font-weight: bold; -fx-text-fill: " + obterCor(resultado) + ";");
        lblTitulo.setWrapText(true);
        lblTitulo.setAlignment(Pos.CENTER);
        lblTitulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        lblSubtitulo = new Label(obterSubtitulo(resultado));
        lblSubtitulo.getStyleClass().add("card-text");
        lblSubtitulo.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255,255,255,0.8);");
        lblSubtitulo.setWrapText(true);
        lblSubtitulo.setAlignment(Pos.CENTER);
        lblSubtitulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblSubtitulo.setMaxWidth(520);

        btnSimularResto = new Button("Simular o restante da Copa");
        btnSimularResto.getStyleClass().add("primary-button");
        btnSimularResto.setOnAction(event -> simularRestanteDaCopa());

        btnChaveamento = new Button("Ver chaveamento completo");
        btnChaveamento.getStyleClass().add("secondary-button");
        btnChaveamento.setOnAction(event -> navigator.showTabelasChaveamento());

        btnHub = new Button("Ir para o Hub");
        btnHub.getStyleClass().add("ghost-button");
        btnHub.setOnAction(event -> navigator.showHub());

        HBox botoes = new HBox(12, btnSimularResto, btnChaveamento, btnHub);
        botoes.setAlignment(Pos.CENTER);

        cartao = new VBox(18);
        cartao.setAlignment(Pos.CENTER);
        cartao.getStyleClass().add("hero-panel");
        cartao.setPadding(new Insets(48));
        cartao.setMaxWidth(640);
        cartao.setMaxHeight(VBox.USE_PREF_SIZE);

        if (bandeira != null) {
            cartao.getChildren().add(bandeira);
        }
        cartao.getChildren().addAll(lblTitulo, lblSubtitulo, botoes);

        VBox wrapper = new VBox(cartao);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(40));
        root.setCenter(wrapper);

        atualizarVisibilidadeBotaoSimular();
        tocarSomDoResultado(resultado);
    }

    /**
     * Simula dia a dia o restante do calendário até a Copa terminar e revela o
     * campeão final na própria tela.
     */
    private void simularRestanteDaCopa() {
        GerenciadorTorneio gerenciadorTorneio = navigator.getSession().getGerenciadorTorneio();

        for (int dia = 1; dia <= 39; dia++) {
            if (gerenciadorTorneio.getFaseAtual() == FaseTorneio.ENCERRADO) {
                break;
            }

            gerenciadorTorneio.simularPartidasDoDia(dia);

            if (gerenciadorTorneio.getFaseAtual() == FaseTorneio.FASE_DE_GRUPOS && gerenciadorTorneio.isFaseGruposConcluida()) {
                gerenciadorTorneio.iniciarMataMata();
            }
        }

        exibirCampeaoFinal(gerenciadorTorneio);
    }

    /**
     * Acrescenta o campeão do torneio ao rodapé da tela e esconde o botão de simulação,
     * já que não há mais nada para avançar.
     */
    private void exibirCampeaoFinal(GerenciadorTorneio gerenciadorTorneio) {
        Time campeao = gerenciadorTorneio.getCampeao();
        String nomeCampeao = campeao == null ? "" : formatarNomePais(campeao.getNome());

        Label lblCampeao = new Label("🏆 Campeão da Copa de 2026: " + nomeCampeao);
        lblCampeao.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f0d58b;");
        lblCampeao.setWrapText(true);

        cartao.getChildren().add(cartao.getChildren().size() - 1, lblCampeao);
        atualizarVisibilidadeBotaoSimular();
    }

    private void atualizarVisibilidadeBotaoSimular() {
        GerenciadorTorneio gerenciadorTorneio = navigator.getSession().getGerenciadorTorneio();
        boolean copaEmAndamento = gerenciadorTorneio.getFaseAtual() != FaseTorneio.ENCERRADO;
        btnSimularResto.setVisible(copaEmAndamento);
        btnSimularResto.setManaged(copaEmAndamento);
    }

    private void tocarSomDoResultado(String resultado) {
        TocadorDeSons tocadorDeSons = new TocadorDeSons();
        if ("CAMPEAO".equals(resultado)) {
            tocadorDeSons.tocarGol();
        } else {
            tocadorDeSons.tocarLesao();
        }
    }

    private String obterTitulo(String resultado) {
        switch (resultado) {
            case "CAMPEAO":
                return "🏆 CAMPEÃO DO MUNDO!";
            case "VICE_CAMPEAO":
                return "🥈 VICE-CAMPEÃO";
            case "TERCEIRO_LUGAR":
                return "🥉 TERCEIRO LUGAR";
            case "QUARTO_LUGAR":
                return "4º LUGAR";
            default:
                return "❌ ELIMINADO";
        }
    }

    private String obterSubtitulo(String resultado) {
        switch (resultado) {
            case "CAMPEAO":
                return "O Brasil conquistou a Copa do Mundo de 2026! A torcida vai à loucura com a sexta estrela.";
            case "VICE_CAMPEAO":
                return "O Brasil chegou até a final, mas não conseguiu levantar a taça dessa vez.";
            case "TERCEIRO_LUGAR":
                return "O Brasil fecha a campanha no pódio, com o terceiro lugar da Copa.";
            case "QUARTO_LUGAR":
                return "O Brasil ficou perto do pódio, mas terminou a campanha em quarto lugar.";
            default:
                return "O Brasil foi eliminado " + nomeDaFaseEliminacao(resultado) + ". A campanha na Copa de 2026 termina por aqui.";
        }
    }

    private String nomeDaFaseEliminacao(String resultado) {
        if (resultado.equals("ELIMINADO_FASE_DE_GRUPOS")) return "na fase de grupos";
        if (resultado.equals("ELIMINADO_DEZESSEIS_AVOS")) return "nos dezesseis-avos de final";
        if (resultado.equals("ELIMINADO_OITAVAS")) return "nas oitavas de final";
        if (resultado.equals("ELIMINADO_QUARTAS")) return "nas quartas de final";
        return "no mata-mata";
    }

    private String obterCor(String resultado) {
        switch (resultado) {
            case "CAMPEAO":
                return "#f0d58b";
            case "VICE_CAMPEAO":
            case "TERCEIRO_LUGAR":
                return "#8bf0a1";
            case "QUARTO_LUGAR":
                return "#f0d58b";
            default:
                return "#ff6b6b";
        }
    }

    private String formatarNomePais(String nomeBruto) {
        if (nomeBruto == null || nomeBruto.isEmpty()) return "";

        if (nomeBruto.equalsIgnoreCase("africa_do_sul")) return "África do Sul";
        if (nomeBruto.equalsIgnoreCase("coreia_do_sul")) return "Coreia do Sul";
        if (nomeBruto.equalsIgnoreCase("coreia_do_norte")) return "Coreia do Norte";
        if (nomeBruto.equalsIgnoreCase("arabia_saudita")) return "Arábia Saudita";
        if (nomeBruto.equalsIgnoreCase("gra_bretanha")) return "Grã-Bretanha";
        if (nomeBruto.equalsIgnoreCase("camaroes")) return "Camarões";
        if (nomeBruto.equalsIgnoreCase("ira")) return "Irã";
        if (nomeBruto.equalsIgnoreCase("japao")) return "Japão";

        String[] partes = nomeBruto.replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();

        for (String p : partes) {
            if (p.length() > 0) {
                if (p.equalsIgnoreCase("de") || p.equalsIgnoreCase("do") || p.equalsIgnoreCase("da") || p.equalsIgnoreCase("dos") || p.equalsIgnoreCase("das")) {
                    sb.append(p.toLowerCase()).append(" ");
                } else {
                    sb.append(p.substring(0, 1).toUpperCase()).append(p.substring(1).toLowerCase()).append(" ");
                }
            }
        }
        return sb.toString().trim();
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
