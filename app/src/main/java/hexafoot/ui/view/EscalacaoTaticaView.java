package hexafoot.ui.view;

import hexafoot.model.Formacao;
import hexafoot.model.Jogador;
import hexafoot.model.Time;
import hexafoot.model.strategy.EstrategiaSimulacao;
import hexafoot.model.strategy.TaticaEquilibrada;
import hexafoot.model.strategy.TaticaOfensiva;
import hexafoot.model.strategy.TaticaRetranca;
import hexafoot.ui.GameNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class EscalacaoTaticaView implements ScreenView {
    private enum Origem {
        CAMPO,
        BANCO
    }

    private final GameNavigator navigator;
    private final BorderPane root;
    private final Time time;
    private final ObservableList<Jogador> titulares;
    private final ObservableList<Jogador> reservas;
    private final Label formacaoLabel;
    private final Label taticaLabel;
    private final VBox campoContainer;
    private final VBox bancoContainer;
    private final VBox painelComparacao;
    private final VBox formacaoBox;
    private final VBox taticaBox;

    private Origem jogadorSelecionadoOrigem = null;
    private Integer jogadorSelecionadoIndice = null;
    private Jogador jogadorSelecionado = null;
    private Jogador jogadorHovered = null;

    private void limparSelecao() {
        jogadorSelecionadoOrigem = null;
        jogadorSelecionadoIndice = null;
        jogadorSelecionado = null;
    }

    public EscalacaoTaticaView(GameNavigator navigator) {
        this.navigator = navigator;
        this.time = navigator.getSession().getElencoBrasil();
        removerJogadoresDuplicados();
        reorganizarTitularesConformeFormacao();

        this.root = new BorderPane();
        this.root.getStyleClass().add("screen-root");

        this.titulares = FXCollections.observableArrayList(time.getTitulares());
        this.reservas = FXCollections.observableArrayList(time.getReservas());
        this.formacaoLabel = new Label();
        this.taticaLabel = new Label();
        this.campoContainer = new VBox(10);
        this.bancoContainer = new VBox(10);
        this.painelComparacao = new VBox(10);
        this.formacaoBox = new VBox(8);
        this.taticaBox = new VBox(8);

        // A MÁGICA DE LAYOUT ACONTECE AQUI:
        // Separamos as secções no Topo, Centro e Base do BorderPane.
        VBox cabecalho = criarCabecalho();
        BorderPane.setMargin(cabecalho, new Insets(16, 24, 8, 24));

        HBox areaPrincipal = criarAreaPrincipal();
        BorderPane.setMargin(areaPrincipal, new Insets(0, 24, 8, 24));

        VBox rodape = criarRodape();
        BorderPane.setMargin(rodape, new Insets(0, 24, 16, 24));

        this.root.setTop(cabecalho);
        this.root.setCenter(areaPrincipal);
        this.root.setBottom(rodape);

        atualizarTela();
    }

    private VBox criarCabecalho() {
        Label title = new Label("🇧🇷 Escalação e tática");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Escolha os jogadores que ficarão no campo e no banco, ajuste a formação e altere o estilo de jogo.");
        subtitle.getStyleClass().add("page-subtitle");
        subtitle.setWrapText(true);

        HBox status = new HBox(10,
                criarPill("Formação atual"),
                criarPill("Tática atual"),
                criarPill("Análise de jogadores"));

        VBox header = new VBox(8, title, subtitle, status);
        header.getStyleClass().add("hero-panel");
        header.setPadding(new Insets(16, 20, 16, 20)); // Padding otimizado
        return header;
    }

    private HBox criarAreaPrincipal() {
        campoContainer.getStyleClass().add("pitch-panel");
        campoContainer.setPadding(new Insets(14));
        VBox.setVgrow(campoContainer, Priority.ALWAYS);

        bancoContainer.getStyleClass().add("bench-panel");
        bancoContainer.setPadding(new Insets(14));
        VBox.setVgrow(bancoContainer, Priority.ALWAYS);

        painelComparacao.getStyleClass().add("comparison-panel");
        painelComparacao.setPadding(new Insets(16, 12, 16, 12));
        VBox.setVgrow(painelComparacao, Priority.ALWAYS);

        HBox content = new HBox(14, campoContainer, bancoContainer, painelComparacao);
        HBox.setHgrow(campoContainer, Priority.ALWAYS);
        HBox.setHgrow(bancoContainer, Priority.ALWAYS);
        HBox.setHgrow(painelComparacao, Priority.ALWAYS);
        return content;
    }

    private VBox criarRodape() {
        Label formacaoTitle = new Label("Formações");
        formacaoTitle.getStyleClass().add("card-title");
        formacaoLabel.getStyleClass().addAll("card-text", "highlighted-green-text");

        Label taticaTitle = new Label("Estilo de jogo");
        taticaTitle.getStyleClass().add("card-title");
        taticaLabel.getStyleClass().addAll("card-text", "highlighted-green-text");

        formacaoBox.getChildren().addAll(formacaoTitle, formacaoLabel, criarBotoesFormacao());
        taticaBox.getChildren().addAll(taticaTitle, taticaLabel, criarBotoesTatica());
        
        HBox controls = new HBox(18, formacaoBox, taticaBox);
        controls.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(formacaoBox, Priority.ALWAYS);
        HBox.setHgrow(taticaBox, Priority.ALWAYS);

        Button voltar = new Button("Voltar ao hub");
        voltar.getStyleClass().add("ghost-button");
        voltar.setStyle("-fx-min-height: 42px; -fx-padding: 0 24px; -fx-font-weight: bold;");
        voltar.setOnAction(event -> navigator.showHub());

        HBox footerLine = new HBox(14, controls, new Region(), voltar);
        HBox.setHgrow(footerLine.getChildren().get(1), Priority.ALWAYS);
        footerLine.setAlignment(Pos.CENTER_LEFT);

        VBox wrapper = new VBox(12, footerLine);
        wrapper.getStyleClass().add("footer-panel");
        wrapper.setPadding(new Insets(16, 24, 16, 24)); // Padding otimizado para não espremer o campo
        return wrapper;
    }

    private VBox criarBotoesFormacao() {
        FlowPane buttons = new FlowPane(8, 8);
        buttons.setPrefWrapLength(450);
        for (Formacao formacao : Formacao.values()) {
            buttons.getChildren().add(criarBotaoFormacao(formacao));
        }
        return new VBox(10, buttons);
    }

    private Button criarBotaoFormacao(Formacao formacao) {
        Button botao = new Button(formatarFormacao(formacao));
        boolean ativo = time.getFormacaoAtual() == formacao;
        botao.getStyleClass().add(ativo ? "primary-button" : "secondary-button");
        botao.setOnAction(event -> {
            time.setFormacaoAtual(formacao);
            reorganizarTitularesConformeFormacao();
            atualizarTela();
        });
        return botao;
    }

    private VBox criarBotoesTatica() {
        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(
                criarBotaoTatica("Pressão", new TaticaOfensiva()),
                criarBotaoTatica("Posse de bola", new TaticaEquilibrada()),
                criarBotaoTatica("Retranca", new TaticaRetranca()));
        return new VBox(10, buttons);
    }

    private Button criarBotaoTatica(String titulo, EstrategiaSimulacao estrategia) {
        Button botao = new Button(titulo);
        boolean ativo = time.getTaticaAtual().getClass().equals(estrategia.getClass());
        botao.getStyleClass().add(ativo ? "primary-button" : "secondary-button");
        botao.setOnAction(event -> {
            time.setTaticaAtual(estrategia);
            atualizarTela();
        });
        return botao;
    }

    private void atualizarTela() {
        limparSelecao();
        titulares.setAll(time.getTitulares());
        reservas.setAll(time.getReservas());
        formacaoLabel.setText("Escolhida: " + formatarFormacao(time.getFormacaoAtual()));
        taticaLabel.setText("Escolhida: " + formatarTatica(time.getTaticaAtual()));
        campoContainer.getChildren().setAll(criarCampo());
        bancoContainer.getChildren().setAll(criarBanco());

        if (formacaoBox.getChildren().size() > 2) {
            formacaoBox.getChildren().set(2, criarBotoesFormacao());
        }
        if (taticaBox.getChildren().size() > 2) {
            taticaBox.getChildren().set(2, criarBotoesTatica());
        }

        atualizarPainelComparacao();
    }

    private VBox criarCampo() {
        Label titulo = new Label("Campo de jogo");
        titulo.getStyleClass().add("card-title");

        Label dica = new Label("Clique ou arraste os jogadores. O campo segue a formação escolhida.");
        dica.getStyleClass().add("card-text");
        dica.setWrapText(true);

        List<Jogador> titularesOrdenados = new ArrayList<>(time.getTitulares());
        int goleiros = 1;
        int defensores = quantidadeDefensores(time.getFormacaoAtual());
        int meio = quantidadeMeio(time.getFormacaoAtual());
        int atacantes = quantidadeAtacantes(time.getFormacaoAtual());

        List<Jogador> linhaGoleiro = slice(titularesOrdenados, 0, goleiros);
        List<Jogador> linhaDefesa = slice(titularesOrdenados, goleiros, defensores);
        List<Jogador> linhaMeio = slice(titularesOrdenados, goleiros + defensores, meio);
        List<Jogador> linhaAtaque = slice(titularesOrdenados, goleiros + defensores + meio, atacantes);

        VBox linesContainer = new VBox(8); // Espaçamento reduzido
        linesContainer.setPadding(new Insets(12)); // Padding reduzido
        linesContainer.setAlignment(Pos.CENTER);

        linesContainer.getChildren().add(criarLinhaCampo("Ataque", "A", linhaAtaque, goleiros + defensores + meio));
        linesContainer.getChildren().add(criarLinhaCampo("Meio-campo", "M", linhaMeio, goleiros + defensores));
        linesContainer.getChildren().add(criarLinhaCampo("Defesa", "D", linhaDefesa, goleiros));
        linesContainer.getChildren().add(criarLinhaCampo("Goleiro", "G", linhaGoleiro, 0));

        FieldMarkingsPane markings = new FieldMarkingsPane();

        StackPane campoVisual = new StackPane(markings, linesContainer);
        campoVisual.getStyleClass().add("football-field");

        ScrollPane scroll = new ScrollPane(campoVisual);
        scroll.getStyleClass().add("pitch-scroll");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        VBox panel = new VBox(8, titulo, dica, scroll);
        panel.getStyleClass().add("pitch-card");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return panel;
    }

    private VBox criarLinhaCampo(String nomeLinha, String sigla, List<Jogador> jogadores, int indiceBase) {
        Label titulo = new Label(nomeLinha);
        titulo.getStyleClass().add("pitch-row-title");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);

        int quantidade = Math.max(jogadores.size(), quantidadeEsperada(nomeLinha));
        for (int i = 0; i < quantidade; i++) {
            Jogador jogador = i < jogadores.size() ? jogadores.get(i) : null;
            row.getChildren().add(criarSlotJogador(jogador, Origem.CAMPO, indiceBase + i, sigla, true));
        }

        VBox wrapper = new VBox(6, titulo, row);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    private VBox criarBanco() {
        HBox topo = new HBox(12);
        Label titulo = new Label("Banco de reservas");
        titulo.getStyleClass().add("card-title");
        Label resumo = new Label(reservas.size() + " jogadores");
        resumo.getStyleClass().add("card-text");
        topo.getChildren().addAll(titulo, new Region(), resumo);
        HBox.setHgrow(topo.getChildren().get(1), Priority.ALWAYS);

        VBox lista = new VBox(10);
        for (int i = 0; i < reservas.size(); i++) {
            lista.getChildren().add(criarSlotJogador(reservas.get(i), Origem.BANCO, i, "B", false));
        }

        ScrollPane scroll = new ScrollPane(lista);
        scroll.getStyleClass().add("bench-scroll");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        VBox panel = new VBox(12, topo, scroll);
        panel.getStyleClass().add("bench-card");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return panel;
    }

    private Button criarSlotJogador(Jogador jogador, Origem origem, int indice, String sigla, boolean campo) {
        Button botao = new Button();
        
        // Bloquear largura para evitar tremidas horizontais
        double larguraFixa = 115;
        botao.setMinWidth(larguraFixa);
        botao.setPrefWidth(larguraFixa);
        botao.setMaxWidth(larguraFixa);
        
        // Bloquear altura num tamanho otimizado (reduzido de 92 para 70) para caber na janela
        double alturaFixa = campo ? 70 : 66; 
        botao.setMinHeight(alturaFixa);
        botao.setPrefHeight(alturaFixa);
        botao.setMaxHeight(alturaFixa);
        
        botao.setWrapText(true);
        botao.getStyleClass().add(campo ? "field-player-card" : "bench-player-card");
        botao.getStyleClass().add(campo ? "slot-zone-" + sigla.toLowerCase() : "slot-bench");

        if (jogador == null) {
            botao.setText("Vazio\n" + sigla);
            botao.getStyleClass().add("slot-empty");
            botao.setDisable(true);
            return botao;
        }

        botao.setText(campo
                ? sigla + " • " + obterNomeCurto(jogador.getNome()) + "\n" + jogador.getPosicao() + " • " + jogador.getFisico() + "%"
                : obterNomeCurto(jogador.getNome()) + "\n" + jogador.getPosicao() + " • " + jogador.getFisico() + "%");

        registrarArraste(botao, origem, indice);
        registrarAlvo(botao, origem, indice);

        botao.setOnAction(event -> tratarSelecaoClique(origem, indice, botao));
        botao.setOnMouseEntered(event -> tratarMouseEntrouSlot(jogador));
        botao.setOnMouseExited(event -> tratarMouseSaiuSlot());

        return botao;
    }

    private void tratarSelecaoClique(Origem origem, int indice, Button botao) {
        Jogador jogadorClicado = (origem == Origem.CAMPO) ? titulares.get(indice) : reservas.get(indice);

        if (jogadorSelecionadoIndice == null) {
            jogadorSelecionadoOrigem = origem;
            jogadorSelecionadoIndice = indice;
            jogadorSelecionado = jogadorClicado;
            botao.getStyleClass().add("slot-selected");
            atualizarPainelComparacao();
        } else {
            Origem orig = jogadorSelecionadoOrigem;
            int ind = jogadorSelecionadoIndice;
            
            if (orig == origem && ind == indice) {
                atualizarTela();
            } else {
                boolean sucesso = moverJogador(orig, ind, origem, indice);
                atualizarTela();
            }
        }
    }

    private void tratarMouseEntrouSlot(Jogador jogador) {
        this.jogadorHovered = jogador;
        atualizarPainelComparacao();
    }

    private void tratarMouseSaiuSlot() {
        this.jogadorHovered = null;
        atualizarPainelComparacao();
    }

    private void atualizarPainelComparacao() {
        painelComparacao.getChildren().clear();

        Label titulo = new Label("Análise de Jogador");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #f8fbff; -fx-padding: 0 0 8 0;");
        painelComparacao.getChildren().add(titulo);

        if (jogadorSelecionado == null && jogadorHovered == null) {
            Label info = new Label("Clique em um jogador para ver os atributos.\n\nPasse o mouse sobre outro para comparar.");
            info.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255, 255, 255, 0.75); -fx-line-spacing: 5px;");
            info.setWrapText(true);
            painelComparacao.getChildren().add(info);
            return;
        }

        if (jogadorSelecionado != null && jogadorHovered != null && jogadorSelecionado != jogadorHovered) {
            Label sub = new Label("Comparando Atletas");
            sub.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.9); -fx-padding: 0 0 4 0;");
            painelComparacao.getChildren().add(sub);

            Label nome1 = new Label("1. " + jogadorSelecionado.getNome() + " (" + jogadorSelecionado.getPosicao() + ")");
            nome1.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #eff5ff;");

            Label nome2 = new Label("2. " + jogadorHovered.getNome() + " (" + jogadorHovered.getPosicao() + ")");
            nome2.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #8bf0a1;");

            painelComparacao.getChildren().addAll(nome1, nome2);

            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(12);
            grid.setPadding(new Insets(12, 0, 0, 0));
            VBox.setVgrow(grid, Priority.ALWAYS);

            ColumnConstraints colAtrib = new ColumnConstraints(90);
            ColumnConstraints colVal1 = new ColumnConstraints(60);
            ColumnConstraints colVal2 = new ColumnConstraints(60);
            ColumnConstraints colDiff = new ColumnConstraints(55);
            grid.getColumnConstraints().addAll(colAtrib, colVal1, colVal2, colDiff);

            Label hAtrib = new Label("Atributo");
            hAtrib.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.6);");
            
            Label hVal1 = new Label(obterNomeCurto(jogadorSelecionado.getNome()));
            hVal1.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.6);");
            GridPane.setHalignment(hVal1, javafx.geometry.HPos.CENTER);

            Label hVal2 = new Label(obterNomeCurto(jogadorHovered.getNome()));
            hVal2.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #8bf0a1;");
            GridPane.setHalignment(hVal2, javafx.geometry.HPos.CENTER);

            Label hDiff = new Label("Dif.");
            hDiff.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.6);");
            GridPane.setHalignment(hDiff, javafx.geometry.HPos.CENTER);

            grid.add(hAtrib, 0, 0);
            grid.add(hVal1, 1, 0);
            grid.add(hVal2, 2, 0);
            grid.add(hDiff, 3, 0);

            adicionarLinhaGrid(grid, 1, "Ataque", jogadorSelecionado.getAtaque(), jogadorHovered.getAtaque(), false);
            adicionarLinhaGrid(grid, 2, "Defesa", jogadorSelecionado.getDefesa(), jogadorHovered.getDefesa(), false);
            adicionarLinhaGrid(grid, 3, "Físico", jogadorSelecionado.getFisico(), jogadorHovered.getFisico(), false);
            adicionarLinhaGrid(grid, 4, "Estresse", jogadorSelecionado.getEstresse(), jogadorHovered.getEstresse(), true);

            painelComparacao.getChildren().add(grid);

            Label statusLabel = new Label("Status: " + jogadorSelecionado.getStatus() + " vs " + jogadorHovered.getStatus());
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255, 255, 255, 0.85); -fx-padding: 8 0 0 0;");
            painelComparacao.getChildren().add(statusLabel);
        } else {
            Jogador j = (jogadorSelecionado != null) ? jogadorSelecionado : jogadorHovered;
            
            Label sub = new Label((jogadorSelecionado != null) ? "Jogador Selecionado" : "Visualizando Atleta");
            sub.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.9); -fx-padding: 0 0 4 0;");
            
            Label nome = new Label(j.getNome());
            nome.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #8bf0a1;");

            Label posicao = new Label("Posição: " + j.getPosicao());
            posicao.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255, 255, 255, 0.85);");

            Label status = new Label("Status: " + j.getStatus());
            status.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255, 255, 255, 0.85);");

            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(12);
            grid.setPadding(new Insets(12, 0, 0, 0));
            VBox.setVgrow(grid, Priority.ALWAYS);

            ColumnConstraints colAtrib = new ColumnConstraints(110);
            ColumnConstraints colVal = new ColumnConstraints(90);
            grid.getColumnConstraints().addAll(colAtrib, colVal);

            Label hAtrib = new Label("Atributo");
            hAtrib.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.6);");

            Label hVal = new Label("Valor");
            hVal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.6);");

            grid.add(hAtrib, 0, 0);
            grid.add(hVal, 1, 0);

            adicionarLinhaGridSimples(grid, 1, "Ataque", j.getAtaque());
            adicionarLinhaGridSimples(grid, 2, "Defesa", j.getDefesa());
            adicionarLinhaGridSimples(grid, 3, "Físico", j.getFisico());
            adicionarLinhaGridSimples(grid, 4, "Estresse", j.getEstresse());

            painelComparacao.getChildren().addAll(sub, nome, posicao, status, grid);
        }
    }

    private String obterNomeCurto(String nomeCompleto) {
        if (nomeCompleto == null) return "";
        String[] partes = nomeCompleto.split(" ");
        if (partes.length > 0) {
            return partes[0];
        }
        return nomeCompleto;
    }

    private void adicionarLinhaGrid(GridPane grid, int row, String atributo, int val1, int val2, boolean menorMelhor) {
        Label labelAtrib = new Label(atributo);
        labelAtrib.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #eff5ff;");

        Label labelVal1 = new Label(String.valueOf(val1));
        labelVal1.setStyle("-fx-font-size: 14px; -fx-text-fill: #f8fbff;");
        GridPane.setHalignment(labelVal1, javafx.geometry.HPos.CENTER);

        Label labelVal2 = new Label(String.valueOf(val2));
        labelVal2.setStyle("-fx-font-size: 14px; -fx-text-fill: #f8fbff;");
        GridPane.setHalignment(labelVal2, javafx.geometry.HPos.CENTER);

        int diff = val2 - val1;
        Label labelDiff = new Label();
        if (diff > 0) {
            labelDiff.setText("+" + diff);
            labelDiff.setStyle(menorMelhor ? "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;" : "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #8bf0a1;");
        } else if (diff < 0) {
            labelDiff.setText(String.valueOf(diff));
            labelDiff.setStyle(menorMelhor ? "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #8bf0a1;" : "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");
        } else {
            labelDiff.setText("=");
            labelDiff.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #808080;");
        }
        GridPane.setHalignment(labelDiff, javafx.geometry.HPos.CENTER);

        grid.add(labelAtrib, 0, row);
        grid.add(labelVal1, 1, row);
        grid.add(labelVal2, 2, row);
        grid.add(labelDiff, 3, row);
    }

    private void adicionarLinhaGridSimples(GridPane grid, int row, String atributo, Object valor) {
        Label labelAtrib = new Label(atributo);
        labelAtrib.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #eff5ff;");

        Label labelVal = new Label(String.valueOf(valor));
        labelVal.setStyle("-fx-font-size: 14px; -fx-text-fill: #f8fbff; -fx-font-weight: bold;");

        grid.add(labelAtrib, 0, row);
        grid.add(labelVal, 1, row);
    }

    private void registrarArraste(Button botao, Origem origem, int indice) {
        botao.setOnDragDetected(event -> {
            Dragboard dragboard = botao.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(origem.name() + ":" + indice);
            dragboard.setContent(content);
            event.consume();
        });
    }

    private void registrarAlvo(Button botao, Origem destino, int indiceDestino) {
        botao.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        botao.setOnDragEntered(event -> botao.getStyleClass().add("slot-drag-over"));
        botao.setOnDragExited(event -> botao.getStyleClass().remove("slot-drag-over"));

        botao.setOnDragDropped(event -> {
            boolean sucesso = false;
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString()) {
                String[] partes = dragboard.getString().split(":");
                if (partes.length == 2) {
                    Origem origem = Origem.valueOf(partes[0]);
                    int indiceOrigem = Integer.parseInt(partes[1]);
                    sucesso = moverJogador(origem, indiceOrigem, destino, indiceDestino);
                }
            }
            event.setDropCompleted(sucesso);
            event.consume();
            if (sucesso) {
                atualizarTela();
            }
        });
    }

    private boolean moverJogador(Origem origem, int indiceOrigem, Origem destino, int indiceDestino) {
        if (origem == destino && indiceOrigem == indiceDestino) {
            return false;
        }

        List<Jogador> titularesAtuais = time.getTitulares();
        List<Jogador> reservasAtuais = time.getReservas();

        if (origem == Origem.CAMPO && destino == Origem.CAMPO) {
            if (indiceOrigem >= titularesAtuais.size() || indiceDestino >= titularesAtuais.size()) {
                return false;
            }
            Jogador origemJogador = titularesAtuais.get(indiceOrigem);
            Jogador destinoJogador = titularesAtuais.get(indiceDestino);
            titularesAtuais.set(indiceOrigem, destinoJogador);
            titularesAtuais.set(indiceDestino, origemJogador);
            return true;
        }

        if (origem == Origem.BANCO && destino == Origem.BANCO) {
            if (indiceOrigem >= reservasAtuais.size() || indiceDestino >= reservasAtuais.size()) {
                return false;
            }
            Jogador origemJogador = reservasAtuais.get(indiceOrigem);
            Jogador destinoJogador = reservasAtuais.get(indiceDestino);
            reservasAtuais.set(indiceOrigem, destinoJogador);
            reservasAtuais.set(indiceDestino, origemJogador);
            return true;
        }

        if (origem == Origem.CAMPO && destino == Origem.BANCO) {
            if (indiceOrigem >= titularesAtuais.size() || indiceDestino >= reservasAtuais.size()) {
                return false;
            }
            Jogador jogadorCampo = titularesAtuais.get(indiceOrigem);
            Jogador jogadorBanco = reservasAtuais.get(indiceDestino);
            titularesAtuais.set(indiceOrigem, jogadorBanco);
            reservasAtuais.set(indiceDestino, jogadorCampo);
            return true;
        }

        if (origem == Origem.BANCO && destino == Origem.CAMPO) {
            if (indiceOrigem >= reservasAtuais.size() || indiceDestino >= titularesAtuais.size()) {
                return false;
            }
            Jogador jogadorBanco = reservasAtuais.get(indiceOrigem);
            Jogador jogadorCampo = titularesAtuais.get(indiceDestino);
            reservasAtuais.set(indiceOrigem, jogadorCampo);
            titularesAtuais.set(indiceDestino, jogadorBanco);
            return true;
        }

        return false;
    }

    private void reorganizarTitularesConformeFormacao() {
        List<Jogador> base = new ArrayList<>(time.getTitulares());
        List<Jogador> reorganizados = new ArrayList<>();

        reorganizados.addAll(preencherLinha(base, "Goleiro", 1));
        reorganizados.addAll(preencherLinha(base, "Defensor", quantidadeDefensores(time.getFormacaoAtual())));
        reorganizados.addAll(preencherLinha(base, "Meio-campista", quantidadeMeio(time.getFormacaoAtual())));
        reorganizados.addAll(preencherLinha(base, "Atacante", quantidadeAtacantes(time.getFormacaoAtual())));

        while (reorganizados.size() < 11 && base.isEmpty() == false) {
            reorganizados.add(base.remove(0));
        }

        if (reorganizados.size() > 11) {
            reorganizados = new ArrayList<>(reorganizados.subList(0, 11));
        }

        time.getTitulares().clear();
        time.getTitulares().addAll(reorganizados);
    }

    private void removerJogadoresDuplicados() {
        List<Jogador> titularesSemRepeticao = new ArrayList<>();
        List<Jogador> reservasSemRepeticao = new ArrayList<>();

        for (Jogador jogador : time.getTitulares()) {
            if (titularesSemRepeticao.contains(jogador) == false) {
                titularesSemRepeticao.add(jogador);
            }
        }

        for (Jogador jogador : time.getReservas()) {
            if (titularesSemRepeticao.contains(jogador) == false && reservasSemRepeticao.contains(jogador) == false) {
                reservasSemRepeticao.add(jogador);
            }
        }

        while (titularesSemRepeticao.size() < 11 && reservasSemRepeticao.isEmpty() == false) {
            titularesSemRepeticao.add(reservasSemRepeticao.remove(0));
        }

        time.getTitulares().clear();
        time.getTitulares().addAll(titularesSemRepeticao);
        time.getReservas().clear();
        time.getReservas().addAll(reservasSemRepeticao);
    }

    private List<Jogador> preencherLinha(List<Jogador> origem, String posicao, int quantidade) {
        List<Jogador> selecionados = new ArrayList<>();
        List<Jogador> copia = new ArrayList<>(origem);

        for (Jogador jogador : copia) {
            if (selecionados.size() == quantidade) {
                break;
            }
            if (normalizarPosicao(jogador.getPosicao()).equals(normalizarPosicao(posicao))) {
                selecionados.add(jogador);
                origem.remove(jogador);
            }
        }

        while (selecionados.size() < quantidade && origem.isEmpty() == false) {
            selecionados.add(origem.remove(0));
        }

        return selecionados;
    }

    private List<Jogador> slice(List<Jogador> lista, int inicio, int quantidade) {
        List<Jogador> resultado = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            int indice = inicio + i;
            if (indice < lista.size()) {
                resultado.add(lista.get(indice));
            } else {
                resultado.add(null);
            }
        }
        return resultado;
    }

    private int quantidadeEsperada(String linha) {
        if ("Ataque".equals(linha)) {
            return quantidadeAtacantes(time.getFormacaoAtual());
        }
        if ("Meio-campo".equals(linha)) {
            return quantidadeMeio(time.getFormacaoAtual());
        }
        if ("Defesa".equals(linha)) {
            return quantidadeDefensores(time.getFormacaoAtual());
        }
        return 1;
    }

    private int quantidadeDefensores(Formacao formacao) {
        switch (formacao) {
            case F_5_4_1:
            case F_5_3_2:
                return 5;
            case F_3_4_3:
            case F_3_5_2:
                return 3;
            case F_4_3_3:
            case F_4_2_4:
            case F_4_4_2:
            case F_4_2_3_1:
            case F_4_5_1:
            default:
                return 4;
        }
    }

    private int quantidadeMeio(Formacao formacao) {
        switch (formacao) {
            case F_4_2_4:
                return 2;
            case F_4_3_3:
            case F_5_3_2:
                return 3;
            case F_3_4_3:
            case F_4_4_2:
            case F_5_4_1:
                return 4;
            case F_4_2_3_1:
            case F_3_5_2:
            case F_4_5_1:
                return 5;
            default:
                return 4;
        }
    }

    private int quantidadeAtacantes(Formacao formacao) {
        switch (formacao) {
            case F_4_2_3_1:
            case F_5_4_1:
            case F_4_5_1:
                return 1;
            case F_4_4_2:
            case F_3_5_2:
            case F_5_3_2:
                return 2;
            case F_4_3_3:
            case F_3_4_3:
                return 3;
            case F_4_2_4:
                return 4;
            default:
                return 2;
        }
    }

    private String normalizarPosicao(String posicao) {
        String limpa = posicao == null ? "" : posicao.trim().toLowerCase();
        if (limpa.contains("gole")) {
            return "goleiro";
        }
        if (limpa.contains("def")) {
            return "defensor";
        }
        if (limpa.contains("meio")) {
            return "meio-campista";
        }
        return "atacante";
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

    private Label criarPill(String texto) {
        Label pill = new Label(texto);
        pill.getStyleClass().add("status-pill");
        return pill;
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    private static class FieldMarkingsPane extends Pane {
        private final List<Rectangle> stripes = new ArrayList<>();
        private final Rectangle boundary = new Rectangle();
        private final Line centerLine = new Line();
        private final Circle centerCircle = new Circle();
        private final Circle centerSpot = new Circle();
        
        // Bottom penalty area
        private final Rectangle bottomPenaltyBox = new Rectangle();
        private final Rectangle bottomGoalBox = new Rectangle();
        private final Arc bottomPenaltyArc = new Arc();
        
        // Top penalty area
        private final Rectangle topPenaltyBox = new Rectangle();
        private final Rectangle topGoalBox = new Rectangle();
        private final Arc topPenaltyArc = new Arc();

        public FieldMarkingsPane() {
            String strokeStyle = "rgba(255, 255, 255, 0.28)";
            double strokeWidth = 2.0;

            // Alternating grass stripes (10 stripes)
            for (int i = 0; i < 10; i++) {
                Rectangle stripe = new Rectangle();
                stripe.setFill(i % 2 == 0 
                    ? javafx.scene.paint.Color.web("rgba(255, 255, 255, 0.03)") 
                    : javafx.scene.paint.Color.web("rgba(0, 0, 0, 0.03)"));
                stripes.add(stripe);
                getChildren().add(stripe);
            }

            setupShape(boundary, strokeStyle, strokeWidth, true);
            setupShape(centerLine, strokeStyle, strokeWidth, false);
            setupShape(centerCircle, strokeStyle, strokeWidth, true);
            setupShape(centerSpot, strokeStyle, strokeWidth, false);
            centerSpot.setFill(javafx.scene.paint.Color.web("rgba(255, 255, 255, 0.4)"));

            setupShape(bottomPenaltyBox, strokeStyle, strokeWidth, true);
            setupShape(bottomGoalBox, strokeStyle, strokeWidth, true);
            setupShape(bottomPenaltyArc, strokeStyle, strokeWidth, false);

            setupShape(topPenaltyBox, strokeStyle, strokeWidth, true);
            setupShape(topGoalBox, strokeStyle, strokeWidth, true);
            setupShape(topPenaltyArc, strokeStyle, strokeWidth, false);

            getChildren().addAll(
                boundary, centerLine, centerCircle, centerSpot,
                bottomPenaltyBox, bottomGoalBox, bottomPenaltyArc,
                topPenaltyBox, topGoalBox, topPenaltyArc
            );

            widthProperty().addListener((obs, oldVal, newVal) -> redimensionar());
            heightProperty().addListener((obs, oldVal, newVal) -> redimensionar());
        }

        private void setupShape(Shape shape, String strokeWebColor, double width, boolean transparentFill) {
            shape.setStroke(javafx.scene.paint.Color.web(strokeWebColor));
            shape.setStrokeWidth(width);
            if (transparentFill) {
                shape.setFill(javafx.scene.paint.Color.TRANSPARENT);
            }
        }

        private void redimensionar() {
            double w = getWidth();
            double h = getHeight();
            if (w <= 0 || h <= 0) return;

            double margin = 12;
            double fieldW = w - 2 * margin;
            double fieldH = h - 2 * margin;

            // Grass stripes
            double stripeH = fieldH / 10;
            for (int i = 0; i < 10; i++) {
                Rectangle stripe = stripes.get(i);
                stripe.setX(margin);
                stripe.setY(margin + i * stripeH);
                stripe.setWidth(fieldW);
                stripe.setHeight(stripeH);
            }

            boundary.setX(margin);
            boundary.setY(margin);
            boundary.setWidth(fieldW);
            boundary.setHeight(fieldH);

            double midX = w / 2;
            double midY = h / 2;

            centerLine.setStartX(margin);
            centerLine.setStartY(midY);
            centerLine.setEndX(w - margin);
            centerLine.setEndY(midY);

            double circleRadius = Math.min(fieldW * 0.18, 60);
            centerCircle.setCenterX(midX);
            centerCircle.setCenterY(midY);
            centerCircle.setRadius(circleRadius);

            centerSpot.setCenterX(midX);
            centerSpot.setCenterY(midY);
            centerSpot.setRadius(3.5);

            // Penalty areas dimensions
            double penaltyW = fieldW * 0.58;
            double penaltyH = fieldH * 0.16;
            double goalW = fieldW * 0.28;
            double goalH = fieldH * 0.055;

            // Bottom
            bottomPenaltyBox.setX(midX - penaltyW / 2);
            bottomPenaltyBox.setY(h - margin - penaltyH);
            bottomPenaltyBox.setWidth(penaltyW);
            bottomPenaltyBox.setHeight(penaltyH);

            bottomGoalBox.setX(midX - goalW / 2);
            bottomGoalBox.setY(h - margin - goalH);
            bottomGoalBox.setWidth(goalW);
            bottomGoalBox.setHeight(goalH);

            bottomPenaltyArc.setCenterX(midX);
            bottomPenaltyArc.setCenterY(h - margin - penaltyH);
            bottomPenaltyArc.setRadiusX(circleRadius * 0.8);
            bottomPenaltyArc.setRadiusY(circleRadius * 0.8);
            bottomPenaltyArc.setStartAngle(0);
            bottomPenaltyArc.setLength(180);

            // Top
            topPenaltyBox.setX(midX - penaltyW / 2);
            topPenaltyBox.setY(margin);
            topPenaltyBox.setWidth(penaltyW);
            topPenaltyBox.setHeight(penaltyH);

            topGoalBox.setX(midX - goalW / 2);
            topGoalBox.setY(margin);
            topGoalBox.setWidth(goalW);
            topGoalBox.setHeight(goalH);

            topPenaltyArc.setCenterX(midX);
            topPenaltyArc.setCenterY(margin + penaltyH);
            topPenaltyArc.setRadiusX(circleRadius * 0.8);
            topPenaltyArc.setRadiusY(circleRadius * 0.8);
            topPenaltyArc.setStartAngle(180);
            topPenaltyArc.setLength(180);
        }
    }
}