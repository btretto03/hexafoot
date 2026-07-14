package hexafoot.ui.view;

import hexafoot.model.FaseTorneio;
import hexafoot.model.Grupo;
import hexafoot.model.Partida;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.StatusPartidaTorneio;
import hexafoot.model.Time;
import hexafoot.service.torneio.GerenciadorTorneio;
import hexafoot.ui.GameNavigator;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class TabelasChaveamentoView implements ScreenView {
    private final BorderPane root;

    public TabelasChaveamentoView(GameNavigator navigator) {
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        GerenciadorTorneio gerenciadorTorneio = navigator.getSession().getGerenciadorTorneio();

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(24));

        Label title = new Label("Tabelas e Chaveamento");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Acompanhe a classificação dos grupos e todos os confrontos do mata-mata.");
        subtitle.getStyleClass().add("page-subtitle");
        subtitle.setWrapText(true);

        Button voltar = new Button("Voltar ao hub");
        voltar.getStyleClass().add("primary-button");
        voltar.setOnAction(event -> navigator.showHub());

        VBox header = new VBox(12, title, subtitle, voltar);
        header.getStyleClass().add("hero-panel");
        header.setPadding(new Insets(24));

        VBox groupsContainer = new VBox(14);
        groupsContainer.getStyleClass().add("groups-container");

        for (Grupo grupo : gerenciadorTorneio.getGrupos()) {
            List<Time> classificacao = gerenciadorTorneio.getClassificacaoGrupo(grupo.getIdentificador());
            groupsContainer.getChildren().add(criarCartaoGrupo("Grupo " + grupo.getIdentificador(), classificacao));
        }

        ScrollPane gruposScroll = criarScrollPane(groupsContainer);
        VBox mataMataContainer = criarChaveamentoMataMata(gerenciadorTorneio);
        ScrollPane mataMataScroll = criarScrollPane(mataMataContainer);

        Tab gruposTab = new Tab("Grupos", gruposScroll);
        Tab mataMataTab = new Tab("Mata-mata", mataMataScroll);
        TabPane abas = new TabPane(gruposTab, mataMataTab);
        abas.getStyleClass().add("tournament-tabs");
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        if (gerenciadorTorneio.getFaseAtual() == FaseTorneio.ENCERRADO) {
            abas.getSelectionModel().select(mataMataTab);
        }

        VBox.setVgrow(abas, Priority.ALWAYS);
        layout.getChildren().addAll(header, abas);
        VBox.setVgrow(layout, Priority.ALWAYS);

        root.setCenter(layout);
    }

    private ScrollPane criarScrollPane(VBox conteudo) {
        ScrollPane scrollPane = new ScrollPane(conteudo);
        scrollPane.getStyleClass().add("championship-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private VBox criarChaveamentoMataMata(GerenciadorTorneio gerenciadorTorneio) {
        VBox container = new VBox(16);
        container.getStyleClass().add("groups-container");

        if (gerenciadorTorneio.getFaseAtual() == FaseTorneio.ENCERRADO) {
            Label resultado = new Label("Campeão: " + nomeComBandeira(gerenciadorTorneio.getCampeao()) + " | Terceiro colocado: " + nomeComBandeira(gerenciadorTorneio.getTerceiroColocado()));
            resultado.getStyleClass().add("page-subtitle");
            container.getChildren().add(resultado);
        }

        FaseTorneio[] fases = {FaseTorneio.DEZESSEIS_AVOS, FaseTorneio.OITAVAS, FaseTorneio.QUARTAS, FaseTorneio.SEMIFINAL, FaseTorneio.TERCEIRO_LUGAR, FaseTorneio.FINAL};
        for (FaseTorneio fase : fases) {
            VBox secao = new VBox(10);
            Label titulo = new Label(formatarFase(fase));
            titulo.getStyleClass().add("group-title");
            secao.getChildren().add(titulo);

            for (PartidaTorneio partida : gerenciadorTorneio.getPartidasMataMata()) {
                if (partida.getFase() == fase) {
                    secao.getChildren().add(criarCartaoPartida(partida));
                }
            }

            container.getChildren().add(secao);
        }

        return container;
    }

    private VBox criarCartaoPartida(PartidaTorneio partidaTorneio) {
        String mandante = nomeParticipante(partidaTorneio.getMandante(), partidaTorneio.getIdentificadorOrigemMandante());
        String visitante = nomeParticipante(partidaTorneio.getVisitante(), partidaTorneio.getIdentificadorOrigemVisitante());

        Label confronto = new Label(partidaTorneio.getId() + " | " + mandante + " x " + visitante);
        confronto.getStyleClass().add("card-title");

        String detalhes = "Agendada";
        if (partidaTorneio.getStatus() == StatusPartidaTorneio.EM_ANDAMENTO) {
            detalhes = "Em andamento";
        } else if (partidaTorneio.getStatus() == StatusPartidaTorneio.CONCLUIDA) {
            Partida partida = partidaTorneio.getPartida();
            detalhes = "Placar: " + partida.getGolsMandante() + " x " + partida.getGolsVisitante() + " | Vencedor: " + nomeComBandeira(partidaTorneio.getVencedor());
        }

        Label resultado = new Label(detalhes);
        resultado.getStyleClass().add("card-text");

        VBox card = new VBox(5, confronto, resultado);
        card.getStyleClass().add("group-card");
        card.setPadding(new Insets(12));
        return card;
    }

    private String nomeParticipante(Time time, String origem) {
        if (time != null) {
            return nomeComBandeira(time);
        }

        return origem;
    }

    private String nomeComBandeira(Time time) {
        return obterBandeira(time.getNome()) + " " + formatarNomePais(time.getNome());
    }

    private String obterBandeira(String nomeBruto) {
        if (nomeBruto == null) return "";
        String nome = nomeBruto.trim().toLowerCase();

        if (nome.equals("africa_do_sul")) return "🇿🇦";
        if (nome.equals("alemanha")) return "🇩🇪";
        if (nome.equals("arabia_saudita")) return "🇸🇦";
        if (nome.equals("argelia")) return "🇩🇿";
        if (nome.equals("argentina")) return "🇦🇷";
        if (nome.equals("australia")) return "🇦🇺";
        if (nome.equals("austria")) return "🇦🇹";
        if (nome.equals("belgica")) return "🇧🇪";
        if (nome.equals("bosnia_e_herzegovina")) return "🇧🇦";
        if (nome.equals("brasil")) return "🇧🇷";
        if (nome.equals("cabo_verde")) return "🇨🇻";
        if (nome.equals("camaroes")) return "🇨🇲";
        if (nome.equals("canada")) return "🇨🇦";
        if (nome.equals("catar")) return "🇶🇦";
        if (nome.equals("colombia")) return "🇨🇴";
        if (nome.equals("coreia_do_sul")) return "🇰🇷";
        if (nome.equals("costa_do_marfim")) return "🇨🇮";
        if (nome.equals("croacia")) return "🇭🇷";
        if (nome.equals("curacau")) return "🇨🇼";
        if (nome.equals("dinamarca")) return "🇩🇰";
        if (nome.equals("egito")) return "🇪🇬";
        if (nome.equals("equador")) return "🇪🇨";
        if (nome.equals("escocia")) return "🏴";
        if (nome.equals("espanha")) return "🇪🇸";
        if (nome.equals("estados_unidos")) return "🇺🇸";
        if (nome.equals("franca")) return "🇫🇷";
        if (nome.equals("gana")) return "🇬🇭";
        if (nome.equals("haiti")) return "🇭🇹";
        if (nome.equals("holanda")) return "🇳🇱";
        if (nome.equals("inglaterra")) return "🏴";
        if (nome.equals("ira")) return "🇮🇷";
        if (nome.equals("iraque")) return "🇮🇶";
        if (nome.equals("japao")) return "🇯🇵";
        if (nome.equals("jordania")) return "🇯🇴";
        if (nome.equals("marrocos")) return "🇲🇦";
        if (nome.equals("mexico")) return "🇲🇽";
        if (nome.equals("noruega")) return "🇳🇴";
        if (nome.equals("nova_zelandia")) return "🇳🇿";
        if (nome.equals("panama")) return "🇵🇦";
        if (nome.equals("paraguai")) return "🇵🇾";
        if (nome.equals("portugal")) return "🇵🇹";
        if (nome.equals("republica_democratica_do_congo")) return "🇨🇩";
        if (nome.equals("republica_tcheca")) return "🇨🇿";
        if (nome.equals("senegal")) return "🇸🇳";
        if (nome.equals("suecia")) return "🇸🇪";
        if (nome.equals("suica")) return "🇨🇭";
        if (nome.equals("tunisia")) return "🇹🇳";
        if (nome.equals("turquia")) return "🇹🇷";
        if (nome.equals("uruguai")) return "🇺🇾";
        if (nome.equals("uzbequistao")) return "🇺🇿";

        return "🏳";
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

    private String formatarFase(FaseTorneio fase) {
        if (fase == FaseTorneio.DEZESSEIS_AVOS) {
            return "Dezesseis-avos";
        }
        if (fase == FaseTorneio.OITAVAS) {
            return "Oitavas de final";
        }
        if (fase == FaseTorneio.QUARTAS) {
            return "Quartas de final";
        }
        if (fase == FaseTorneio.SEMIFINAL) {
            return "Semifinais";
        }
        if (fase == FaseTorneio.TERCEIRO_LUGAR) {
            return "Terceiro lugar";
        }

        return "Final";
    }

    private VBox criarCartaoGrupo(String grupo, List<Time> times) {
        Label label = new Label(grupo);
        label.getStyleClass().add("group-title");

        TableView<Time> tabela = criarTabelaClassificacao(times);
        VBox card = new VBox(10, label, tabela);
        card.getStyleClass().add("group-card");
        card.setPadding(new Insets(16));
        return card;
    }

    private TableView<Time> criarTabelaClassificacao(List<Time> times) {
        TableView<Time> tabela = new TableView<>();
        tabela.getStyleClass().add("group-table");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabela.setItems(javafx.collections.FXCollections.observableArrayList(new ArrayList<>(times)));
        tabela.setPrefHeight(220);
        tabela.setMinHeight(220);
        tabela.setMaxHeight(220);

        tabela.getColumns().add(criarColunaTexto("Time", this::nomeComBandeira, 1.8));
        tabela.getColumns().add(criarColunaInteiro("P", Time::getPontos, 0.6));
        tabela.getColumns().add(criarColunaInteiro("J", time -> time.getVitorias() + time.getEmpates() + time.getDerrotas(), 0.6));
        tabela.getColumns().add(criarColunaInteiro("V", Time::getVitorias, 0.6));
        tabela.getColumns().add(criarColunaInteiro("E", Time::getEmpates, 0.6));
        tabela.getColumns().add(criarColunaInteiro("D", Time::getDerrotas, 0.6));
        tabela.getColumns().add(criarColunaInteiro("GP", Time::getGolsMarcados, 0.7));
        tabela.getColumns().add(criarColunaInteiro("GC", Time::getGolsSofridos, 0.7));
        tabela.getColumns().add(criarColunaInteiro("SG", Time::getSaldoGols, 0.7));

        return tabela;
    }

    private TableColumn<Time, String> criarColunaTexto(String titulo, java.util.function.Function<Time, String> extrator, double largura) {
        TableColumn<Time, String> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(celula -> new ReadOnlyStringWrapper(extrator.apply(celula.getValue())));
        coluna.setPrefWidth(largura * 100);
        return coluna;
    }

    private TableColumn<Time, Number> criarColunaInteiro(String titulo, java.util.function.ToIntFunction<Time> extrator, double largura) {
        TableColumn<Time, Number> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(celula -> new ReadOnlyIntegerWrapper(extrator.applyAsInt(celula.getValue())));
        coluna.setPrefWidth(largura * 100);
        return coluna;
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}