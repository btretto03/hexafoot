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

    public EscalacaoTaticaView(GameNavigator navigator) {
        this.navigator = navigator;
        this.time = navigator.getSession().getElencoBrasil();
        reorganizarTitularesConformeFormacao();

        this.root = new BorderPane();
        this.root.getStyleClass().add("screen-root");

        this.titulares = FXCollections.observableArrayList(time.getTitulares());
        this.reservas = FXCollections.observableArrayList(time.getReservas());
        this.formacaoLabel = new Label();
        this.taticaLabel = new Label();
        this.campoContainer = new VBox(14);
        this.bancoContainer = new VBox(14);

        VBox layout = new VBox(18);
        layout.setPadding(new Insets(24));
        layout.getChildren().addAll(criarCabecalho(), criarAreaPrincipal(), criarRodape());
        VBox.setVgrow(layout.getChildren().get(1), Priority.ALWAYS);

        root.setCenter(layout);
        atualizarTela();
    }

    private VBox criarCabecalho() {
        Label title = new Label("Escalação e tática");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Arraste os jogadores entre o campo e o banco, ajuste a formação e altere o estilo de jogo sem sair da central do técnico.");
        subtitle.getStyleClass().add("page-subtitle");
        subtitle.setWrapText(true);

        HBox status = new HBox(10,
                criarPill("Formação atual"),
                criarPill("Tática atual"),
                criarPill("Drag and drop ativo"));

        VBox header = new VBox(12, title, subtitle, status);
        header.getStyleClass().add("hero-panel");
        header.setPadding(new Insets(24));
        return header;
    }

    private HBox criarAreaPrincipal() {
        campoContainer.getStyleClass().add("pitch-panel");
        campoContainer.setPadding(new Insets(22));
        VBox.setVgrow(campoContainer, Priority.ALWAYS);

        bancoContainer.getStyleClass().add("bench-panel");
        bancoContainer.setPadding(new Insets(22));
        VBox.setVgrow(bancoContainer, Priority.ALWAYS);

        HBox content = new HBox(18, campoContainer, bancoContainer);
        HBox.setHgrow(campoContainer, Priority.ALWAYS);
        HBox.setHgrow(bancoContainer, Priority.ALWAYS);
        return content;
    }

    private VBox criarRodape() {
        Label formacaoTitle = new Label("Formações");
        formacaoTitle.getStyleClass().add("card-title");
        formacaoLabel.getStyleClass().add("card-text");

        Label taticaTitle = new Label("Estilo de jogo");
        taticaTitle.getStyleClass().add("card-title");
        taticaLabel.getStyleClass().add("card-text");

        VBox formacaoBox = new VBox(10, formacaoTitle, formacaoLabel, criarBotoesFormacao());
        VBox taticaBox = new VBox(10, taticaTitle, taticaLabel, criarBotoesTatica());
        HBox controls = new HBox(18, formacaoBox, taticaBox);
        controls.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(formacaoBox, Priority.ALWAYS);
        HBox.setHgrow(taticaBox, Priority.ALWAYS);

        Button voltar = new Button("Voltar ao hub");
        voltar.getStyleClass().add("ghost-button");
        voltar.setOnAction(event -> navigator.showHub());

        HBox footerLine = new HBox(14, controls, new Region(), voltar);
        HBox.setHgrow(footerLine.getChildren().get(1), Priority.ALWAYS);
        footerLine.setAlignment(Pos.CENTER_LEFT);

        VBox wrapper = new VBox(16, footerLine);
        wrapper.getStyleClass().add("footer-panel");
        return wrapper;
    }

    private VBox criarBotoesFormacao() {
        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(
                criarBotaoFormacao(Formacao.F_4_3_3),
                criarBotaoFormacao(Formacao.F_4_4_2),
                criarBotaoFormacao(Formacao.F_4_2_3_1),
                criarBotaoFormacao(Formacao.F_3_5_2),
                criarBotaoFormacao(Formacao.F_5_4_1));
        return new VBox(10, buttons);
    }

    private Button criarBotaoFormacao(Formacao formacao) {
        Button botao = new Button(formatarFormacao(formacao));
        botao.getStyleClass().add("secondary-button");
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
        botao.getStyleClass().add("secondary-button");
        botao.setOnAction(event -> {
            time.setTaticaAtual(estrategia);
            atualizarTela();
        });
        return botao;
    }

    private void atualizarTela() {
        titulares.setAll(time.getTitulares());
        reservas.setAll(time.getReservas());
        formacaoLabel.setText("Escolhida: " + formatarFormacao(time.getFormacaoAtual()));
        taticaLabel.setText("Escolhida: " + formatarTatica(time.getTaticaAtual()));
        campoContainer.getChildren().setAll(criarCampo());
        bancoContainer.getChildren().setAll(criarBanco());
    }

    private VBox criarCampo() {
        Label titulo = new Label("Campo de jogo");
        titulo.getStyleClass().add("card-title");

        Label dica = new Label("Arraste os jogadores para trocar posições. O campo segue a formação escolhida e o banco fica à direita.");
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

        VBox campoVisual = new VBox(18);
        campoVisual.getStyleClass().add("football-field");
        campoVisual.setPadding(new Insets(18));

        campoVisual.getChildren().add(criarLinhaCampo("Ataque", "A", linhaAtaque, goleiros + defensores + meio));
        campoVisual.getChildren().add(criarLinhaCampo("Meio-campo", "M", linhaMeio, goleiros + defensores));
        campoVisual.getChildren().add(criarLinhaCampo("Defesa", "D", linhaDefesa, goleiros));
        campoVisual.getChildren().add(criarLinhaCampo("Goleiro", "G", linhaGoleiro, 0));

        ScrollPane scroll = new ScrollPane(campoVisual);
        scroll.getStyleClass().add("pitch-scroll");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        VBox panel = new VBox(12, titulo, dica, scroll);
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

        VBox wrapper = new VBox(8, titulo, row);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    private VBox criarBanco() {
        HBox topo = new HBox(12);
        Label titulo = new Label("Banco de reservas");
        titulo.getStyleClass().add("card-title");
        Label resumo = new Label(reservas.size() + " jogadores disponíveis para troca");
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
        botao.setMaxWidth(Double.MAX_VALUE);
        botao.setMinHeight(campo ? 92 : 76);
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
                ? sigla + " • " + jogador.getNome() + "\n" + jogador.getPosicao() + " • " + jogador.getFisico() + "%"
                : jogador.getNome() + "\n" + jogador.getPosicao() + " • " + jogador.getFisico() + "%");

        registrarArraste(botao, origem, indice);
        registrarAlvo(botao, origem, indice);
        return botao;
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
        if (formacao == Formacao.F_5_4_1 || formacao == Formacao.F_5_3_2) {
            return 5;
        }
        if (formacao == Formacao.F_3_4_3 || formacao == Formacao.F_3_5_2) {
            return 3;
        }
        return 4;
    }

    private int quantidadeMeio(Formacao formacao) {
        if (formacao == Formacao.F_4_3_3 || formacao == Formacao.F_5_3_2) {
            return 3;
        }
        if (formacao == Formacao.F_4_2_4) {
            return 2;
        }
        if (formacao == Formacao.F_4_5_1 || formacao == Formacao.F_3_5_2) {
            return 5;
        }
        if (formacao == Formacao.F_4_2_3_1) {
            return 4;
        }
        return 4;
    }

    private int quantidadeAtacantes(Formacao formacao) {
        if (formacao == Formacao.F_4_3_3 || formacao == Formacao.F_3_4_3) {
            return 3;
        }
        if (formacao == Formacao.F_4_2_4 || formacao == Formacao.F_5_4_1) {
            return 4;
        }
        if (formacao == Formacao.F_5_3_2 || formacao == Formacao.F_4_2_3_1) {
            return 2;
        }
        if (formacao == Formacao.F_4_5_1) {
            return 1;
        }
        return 2;
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
}
