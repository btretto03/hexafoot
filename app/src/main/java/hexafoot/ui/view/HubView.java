package hexafoot.ui.view;

import hexafoot.dados.GerenciadorSalvamento;
import hexafoot.model.FaseTorneio;
import hexafoot.model.Formacao;
import hexafoot.model.Jogador;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.StatusPartidaTorneio;
import hexafoot.model.Time;
import hexafoot.model.strategy.EstrategiaSimulacao;
import hexafoot.model.strategy.TaticaOfensiva;
import hexafoot.model.strategy.TaticaRetranca;
import hexafoot.service.torneio.GerenciadorTorneio;
import hexafoot.ui.GameNavigator;
import javafx.animation.FadeTransition;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Exibe o estado da campanha ativa e conduz o usuário à próxima partida ou aos ajustes do elenco.
 */
public class HubView extends TelaBase {
    private final BorderPane root;
    private Button btnPartidaSimulacao;
    private VBox actionsStack;
    private HBox content;
    private VBox elencoPanel;

    private VBox smallCalendarContainer;
    private HBox smallCalendarRow;
    private Label lblSmallCalendarInfo;

    public HubView(GameNavigator navigator) {
        super(navigator);
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(24));

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);

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

        // Criar Mini-Calendário Superior
        smallCalendarContainer = new VBox(8);
        smallCalendarContainer.setAlignment(Pos.CENTER_RIGHT);
        
        Label lblCronograma = new Label("CRONOGRAMA");
        lblCronograma.getStyleClass().add("eyebrow");
        lblCronograma.setStyle("-fx-text-fill: #8bf0a1;");
        
        smallCalendarRow = new HBox(8);
        smallCalendarRow.setAlignment(Pos.CENTER_RIGHT);
        
        lblSmallCalendarInfo = new Label();
        lblSmallCalendarInfo.getStyleClass().add("card-text");
        lblSmallCalendarInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255, 255, 255, 0.6);");
        
        smallCalendarContainer.getChildren().addAll(lblCronograma, smallCalendarRow, lblSmallCalendarInfo);

        topBar.getChildren().addAll(heroCopy, smallCalendarContainer);
        HBox.setHgrow(heroCopy, Priority.ALWAYS);

        VBox nextStepsPanel = criarMenuProximosPassos(navigator);
        elencoPanel = criarPainelElenco(navigator);

        content = new HBox(18, nextStepsPanel, elencoPanel);
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

        atualizarSmallCalendar();
        atualizarControleSimulacao();
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
        actionsStack = new VBox(12);
        actionsStack.getStyleClass().add("next-steps-stack");

        Label sectionTitle = new Label("Próximos passos");
        sectionTitle.getStyleClass().add("card-title");

        Label sectionText = new Label("Atalhos para seguir a experiência sem perder a sensação de jogo.");
        sectionText.getStyleClass().add("card-text");
        sectionText.setWrapText(true);

        Button escalacao = new Button("Abrir escalação e tática");
        escalacao.getStyleClass().add("secondary-button");
        escalacao.setOnAction(event -> navigator.showEscalacaoTatica());

        // Criar o botão de simulação/partida dinâmico
        btnPartidaSimulacao = new Button();

        Button tabela = new Button("Consultar grupos e mata-mata");
        tabela.getStyleClass().add("secondary-button");
        tabela.setOnAction(event -> navigator.showTabelasChaveamento());

        Button calendario = new Button("Consultar calendário");
        calendario.getStyleClass().add("secondary-button");
        calendario.setOnAction(event -> navigator.showCalendario());

        Button salvar = new Button("Salvar progresso");
        salvar.getStyleClass().add("secondary-button");
        salvar.setOnAction(event -> salvarProgresso(salvar, navigator));

        Button menu = new Button("Voltar ao menu");
        menu.getStyleClass().add("secondary-button");
        menu.setOnAction(event -> navigator.showMainMenu());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        actionsStack.getChildren().addAll(sectionTitle, sectionText, btnPartidaSimulacao, escalacao, tabela, calendario, salvar, spacer, menu);

        VBox panel = new VBox(14, actionsStack);
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

    private int calcularDiaAtual(GerenciadorTorneio gerenciadorTorneio) {
        int menorDiaNaoConcluido = 39;
        boolean encontrou = false;
        
        List<PartidaTorneio> todasPartidas = new ArrayList<>();
        todasPartidas.addAll(gerenciadorTorneio.getPartidasFaseGrupos());
        todasPartidas.addAll(gerenciadorTorneio.getPartidasMataMata());
        
        for (PartidaTorneio partida : todasPartidas) {
            if (partida.getStatus() != StatusPartidaTorneio.CONCLUIDA) {
                int dia = gerenciadorTorneio.getDiaDaPartida(partida);
                if (dia < menorDiaNaoConcluido) {
                    menorDiaNaoConcluido = dia;
                    encontrou = true;
                }
            }
        }
        
        if (!encontrou) {
            return 39;
        }
        
        return menorDiaNaoConcluido;
    }

    private void atualizarSmallCalendar() {
        smallCalendarRow.getChildren().clear();

        GerenciadorTorneio gt = navigator.getSession().getGerenciadorTorneio();
        int diaAtual = calcularDiaAtual(gt);
        
        PartidaTorneio proximaPartidaBrasil = gt.getProximaPartidaBrasil().orElse(null);
        int diaProximoJogo = -1;
        if (proximaPartidaBrasil != null) {
            diaProximoJogo = gt.getDiaDaPartida(proximaPartidaBrasil);
        }

        int startDay = Math.min(diaAtual, 37);
        if (startDay < 1) startDay = 1;

        for (int i = 0; i < 3; i++) {
            int dia = startDay + i;
            
            VBox tile = new VBox(3);
            tile.setAlignment(Pos.CENTER);
            tile.getStyleClass().add("calendar-tile");
            tile.setPadding(new Insets(6));
            tile.setStyle("-fx-min-width: 75; -fx-min-height: 65;");

            LocalDate startDate = LocalDate.of(2026, 6, 11);
            LocalDate date = startDate.plusDays(dia - 1);

            Label lblDiaSemana = new Label(formatarDiaSemana(date.getDayOfWeek()));
            lblDiaSemana.getStyleClass().add("calendar-day-name");
            lblDiaSemana.setStyle("-fx-font-size: 10px;");

            Label lblNumero = new Label(date.format(DateTimeFormatter.ofPattern("dd/MM")));
            lblNumero.getStyleClass().add("calendar-day-number");
            lblNumero.setStyle("-fx-font-size: 14px;");

            tile.getChildren().addAll(lblDiaSemana, lblNumero);

            if (dia > 39) {
                tile.setDisable(true);
                tile.setOpacity(0.3);
                smallCalendarRow.getChildren().add(tile);
                continue;
            }

            if (dia == diaAtual) {
                tile.getStyleClass().add("calendar-tile-today");
                tile.setStyle(tile.getStyle() + " -fx-border-color: #8bf0a1; -fx-border-width: 1.5; -fx-border-radius: 14; -fx-background-radius: 14;");
            }
            
            if (dia == diaProximoJogo) {
                tile.setStyle(tile.getStyle() + " -fx-border-color: #f0d58b; -fx-border-width: 2; -fx-border-radius: 14; -fx-background-radius: 14;");
                Label lblJogo = new Label("SEU JOGO");
                lblJogo.setStyle("-fx-font-size: 8px; -fx-text-fill: #f0d58b; -fx-font-weight: bold;");
                tile.getChildren().add(lblJogo);
            }
            
            if (dia < diaAtual) {
                tile.setStyle(tile.getStyle() + " -fx-background-color: rgba(255, 255, 255, 0.03); -fx-opacity: 0.5;");
            }

            smallCalendarRow.getChildren().add(tile);
        }

        lblSmallCalendarInfo.setText("Dia " + diaAtual + " de 39 · " + formatarFase(gt.getFaseAtual()));
    }

    private void atualizarControleSimulacao() {
        GerenciadorTorneio gt = navigator.getSession().getGerenciadorTorneio();
        PartidaTorneio proxima = gt.getProximaPartidaBrasil().orElse(null);
        
        if (proxima == null) {
            btnPartidaSimulacao.setText("Copa Encerrada");
            btnPartidaSimulacao.getStyleClass().remove("primary-button");
            if (!btnPartidaSimulacao.getStyleClass().contains("secondary-button")) {
                btnPartidaSimulacao.getStyleClass().add("secondary-button");
            }
            btnPartidaSimulacao.setDisable(true);
            return;
        }
        
        int diaAtual = calcularDiaAtual(gt);
        int diaProximoJogo = gt.getDiaDaPartida(proxima);
        
        btnPartidaSimulacao.getStyleClass().remove("primary-button");
        if (!btnPartidaSimulacao.getStyleClass().contains("secondary-button")) {
            btnPartidaSimulacao.getStyleClass().add("secondary-button");
        }
        btnPartidaSimulacao.setDisable(false);
        
        if (diaAtual == diaProximoJogo) {
            btnPartidaSimulacao.setText("Jogar próxima partida");
            btnPartidaSimulacao.setOnAction(event -> navigator.showSimulacaoPartida(proxima));
        } else {
            btnPartidaSimulacao.setText("Simular calendário");
            btnPartidaSimulacao.setOnAction(event -> iniciarSimulacaoCalendario());
        }
    }

    private void iniciarSimulacaoCalendario() {
        setBotoesBloqueados(true);
        
        GerenciadorTorneio gt = navigator.getSession().getGerenciadorTorneio();
        int diaAtual = calcularDiaAtual(gt);
        
        PartidaTorneio proximaPartidaBrasil = gt.getProximaPartidaBrasil().orElse(null);
        int diaProximoJogoBrasil = -1;
        if (proximaPartidaBrasil != null) {
            diaProximoJogoBrasil = gt.getDiaDaPartida(proximaPartidaBrasil);
        }
        
        executarPassoSimulacao(diaAtual, diaProximoJogoBrasil);
    }
    
    private void executarPassoSimulacao(int dia, int diaProximoJogoBrasil) {
        GerenciadorTorneio gt = navigator.getSession().getGerenciadorTorneio();
        
        gt.simularPartidasDoDia(dia);
        
        if (gt.getFaseAtual() == FaseTorneio.FASE_DE_GRUPOS && gt.isFaseGruposConcluida()) {
            gt.iniciarMataMata();
        }
        
        int novoDia = calcularDiaAtual(gt);
        
        animarTransicaoCalendario();
        atualizarPainelElenco();
        
        boolean fimTorneio = gt.getFaseAtual() == FaseTorneio.ENCERRADO;
        boolean chegouNoJogo = (diaProximoJogoBrasil != -1 && novoDia >= diaProximoJogoBrasil);
        
        if (chegouNoJogo || fimTorneio || novoDia == dia) {
            setBotoesBloqueados(false);
            atualizarControleSimulacao();
        } else {
            PauseTransition delay = new PauseTransition(Duration.millis(600));
            delay.setOnFinished(e -> executarPassoSimulacao(novoDia, diaProximoJogoBrasil));
            delay.play();
        }
    }

    private void animarTransicaoCalendario() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), smallCalendarContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);
        fadeOut.setOnFinished(e -> {
            atualizarSmallCalendar();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), smallCalendarContainer);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void setBotoesBloqueados(boolean bloquear) {
        for (javafx.scene.Node node : actionsStack.getChildren()) {
            if (node instanceof Button) {
                node.setDisable(bloquear);
            }
        }
    }

    private void atualizarPainelElenco() {
        content.getChildren().remove(elencoPanel);
        elencoPanel = criarPainelElenco(navigator);
        content.getChildren().add(elencoPanel);
        HBox.setHgrow(elencoPanel, Priority.ALWAYS);
    }

    private String formatarDiaSemana(DayOfWeek day) {
        switch (day) {
            case SUNDAY: return "DOM";
            case MONDAY: return "SEG";
            case TUESDAY: return "TER";
            case WEDNESDAY: return "QUA";
            case THURSDAY: return "QUI";
            case FRIDAY: return "SEX";
            case SATURDAY: return "SÁB";
            default: return "";
        }
    }

    private String formatarFase(FaseTorneio fase) {
        if (fase == FaseTorneio.DEZESSEIS_AVOS) {
            return "Dezesseis-avos de final";
        }
        if (fase == FaseTorneio.OITAVAS) {
            return "Oitavas de final";
        }
        if (fase == FaseTorneio.QUARTAS) {
            return "Quartas de final";
        }
        if (fase == FaseTorneio.SEMIFINAL) {
            return "Semifinal";
        }
        if (fase == FaseTorneio.TERCEIRO_LUGAR) {
            return "Disputa de 3º lugar";
        }
        if (fase == FaseTorneio.FINAL) {
            return "Final";
        }
        if (fase == FaseTorneio.ENCERRADO) {
            return "Encerrado";
        }
        return "Fase de Grupos";
    }

    private String formatarNomePais(String nomeBruto) {
        if (nomeBruto == null || nomeBruto.isEmpty()) return "";

        if (nomeBruto.equalsIgnoreCase("africa_do_sul")) return "África do Sul";
        if (nomeBruto.equalsIgnoreCase("coreia_do_sul")) return "Coreia do Sul";
        if (nomeBruto.equalsIgnoreCase("arabia_saudita")) return "Arábia Saudita";
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
