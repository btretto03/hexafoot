package hexafoot.ui.view;

import hexafoot.model.EventoPartida;
import hexafoot.model.FaseTorneio;
import hexafoot.model.Partida;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.StatusPartidaTorneio;
import hexafoot.model.Time;
import hexafoot.service.torneio.GerenciadorTorneio;
import hexafoot.ui.GameNavigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarioView extends TelaBase {
    private final BorderPane root;
    private int semanaAtual = 1; // 1 a 6
    private int diaSelecionado = 1; // 1 a 39
    private final VBox calendarRowContainer;
    private final VBox matchContainer;
    private final Label lblSemana;

    public CalendarioView(GameNavigator navigator) {
        super(navigator);
        this.root = new BorderPane();
        root.getStyleClass().add("screen-root");

        GerenciadorTorneio gerenciadorTorneio = navigator.getSession().getGerenciadorTorneio();

        // Determinar o dia atual do torneio para inicializar a seleção no dia correto
        int diaAtual = calcularDiaAtual(gerenciadorTorneio);
        this.diaSelecionado = diaAtual;
        this.semanaAtual = (diaSelecionado - 1) / 7 + 1;
        if (semanaAtual > 6) {
            semanaAtual = 6;
        }

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(24));

        // Header
        Label title = new Label("Calendário de Jogos");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Acompanhe o cronograma da Copa, com os confrontos e resultados dia a dia.");
        subtitle.getStyleClass().add("page-subtitle");
        subtitle.setWrapText(true);

        Button voltar = new Button("Voltar ao hub");
        voltar.getStyleClass().add("primary-button");
        voltar.setOnAction(event -> navigator.showHub());

        VBox header = new VBox(12, title, subtitle, voltar);
        header.getStyleClass().add("hero-panel");
        header.setPadding(new Insets(24));

        // Painel do Calendário Semanal
        VBox calendarPanel = new VBox(14);
        calendarPanel.getStyleClass().add("info-card");
        calendarPanel.setPadding(new Insets(20));

        HBox navigationBar = new HBox(15);
        navigationBar.setAlignment(Pos.CENTER);

        Button btnVoltarSemana = new Button("◀");
        btnVoltarSemana.getStyleClass().add("secondary-button");
        btnVoltarSemana.setOnAction(event -> alterarSemana(-1));

        lblSemana = new Label("Semana " + semanaAtual);
        lblSemana.getStyleClass().add("card-title");
        lblSemana.setStyle("-fx-min-width: 120; -fx-alignment: center; -fx-font-size: 16px;");

        Button btnAvancarSemana = new Button("▶");
        btnAvancarSemana.getStyleClass().add("secondary-button");
        btnAvancarSemana.setOnAction(event -> alterarSemana(1));

        navigationBar.getChildren().addAll(btnVoltarSemana, lblSemana, btnAvancarSemana);

        calendarRowContainer = new VBox();
        calendarRowContainer.getStyleClass().add("calendar-row");
        calendarRowContainer.setAlignment(Pos.CENTER);

        calendarPanel.getChildren().addAll(navigationBar, calendarRowContainer);

        // Painel dos Jogos do Dia
        VBox matchesPanel = new VBox(14);
        matchesPanel.getStyleClass().add("info-card");
        matchesPanel.setPadding(new Insets(20));
        VBox.setVgrow(matchesPanel, Priority.ALWAYS);

        Label lblJogosTitulo = new Label("Jogos do Dia");
        lblJogosTitulo.getStyleClass().add("card-title");

        matchContainer = new VBox(10);
        
        ScrollPane scrollMatches = new ScrollPane(matchContainer);
        scrollMatches.getStyleClass().add("championship-scroll");
        scrollMatches.setFitToWidth(true);
        scrollMatches.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        VBox.setVgrow(scrollMatches, Priority.ALWAYS);

        matchesPanel.getChildren().addAll(lblJogosTitulo, scrollMatches);

        layout.getChildren().addAll(header, calendarPanel, matchesPanel);
        VBox.setVgrow(layout, Priority.ALWAYS);

        root.setCenter(layout);

        atualizarCalendario();
        atualizarJogos();
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


    private void alterarSemana(int direcao) {
        int novaSemana = semanaAtual + direcao;
        if (novaSemana >= 1 && novaSemana <= 6) {
            semanaAtual = novaSemana;
            lblSemana.setText("Semana " + semanaAtual);
            atualizarCalendario();
        }
    }

    private void atualizarCalendario() {
        calendarRowContainer.getChildren().clear();
        
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);
        
        GerenciadorTorneio gt = navigator.getSession().getGerenciadorTorneio();
        int diaAtual = calcularDiaAtual(gt);
        
        int diaInicio = (semanaAtual - 1) * 7 + 1;
        
        for (int i = 0; i < 7; i++) {
            int dia = diaInicio + i;
            
            VBox tile = new VBox(5);
            tile.setAlignment(Pos.CENTER);
            tile.getStyleClass().add("calendar-tile");
            tile.setPadding(new Insets(10));
            tile.setCursor(javafx.scene.Cursor.HAND);
            tile.setStyle("-fx-min-width: 90; -fx-min-height: 80;");
            
            LocalDate startDate = LocalDate.of(2026, 6, 11);
            LocalDate date = startDate.plusDays(dia - 1);
            
            Label lblDiaSemana = new Label(formatarDiaSemana(date.getDayOfWeek()));
            lblDiaSemana.getStyleClass().add("calendar-day-name");
            
            Label lblDiaNumero = new Label(date.format(DateTimeFormatter.ofPattern("dd/MM")));
            lblDiaNumero.getStyleClass().add("calendar-day-number");
            
            tile.getChildren().addAll(lblDiaSemana, lblDiaNumero);
            
            if (dia > 39) {
                tile.setDisable(true);
                tile.setOpacity(0.3);
                row.getChildren().add(tile);
                continue;
            }
            
            // Estilizar de acordo com a situação do dia
            if (dia == diaAtual) {
                tile.getStyleClass().add("calendar-tile-today");
            }
            
            if (dia == diaSelecionado) {
                tile.setStyle("-fx-background-color: rgba(139, 240, 161, 0.35); -fx-border-color: #8bf0a1; -fx-border-width: 2; -fx-border-radius: 14; -fx-background-radius: 14;");
            } else if (dia < diaAtual) {
                tile.setStyle("-fx-background-color: rgba(255, 255, 255, 0.03); -fx-opacity: 0.8; -fx-border-color: rgba(255, 255, 255, 0.05); -fx-border-radius: 14; -fx-background-radius: 14;");
            } else {
                tile.setStyle("-fx-background-color: rgba(255, 255, 255, 0.06); -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-radius: 14; -fx-background-radius: 14;");
            }
            
            tile.setOnMouseClicked(event -> {
                diaSelecionado = dia;
                atualizarCalendario();
                atualizarJogos();
            });
            
            row.getChildren().add(tile);
        }
        
        calendarRowContainer.getChildren().add(row);
    }

    private void atualizarJogos() {
        matchContainer.getChildren().clear();
        
        GerenciadorTorneio gerenciadorTorneio = navigator.getSession().getGerenciadorTorneio();
        
        List<PartidaTorneio> todasPartidas = new ArrayList<>();
        todasPartidas.addAll(gerenciadorTorneio.getPartidasFaseGrupos());
        todasPartidas.addAll(gerenciadorTorneio.getPartidasMataMata());
        
        List<PartidaTorneio> partidasDoDia = todasPartidas.stream()
                .filter(p -> gerenciadorTorneio.getDiaDaPartida(p) == diaSelecionado)
                .collect(Collectors.toList());
                
        if (partidasDoDia.isEmpty()) {
            Label lblSemJogos = new Label("Nenhum jogo agendado para este dia.");
            lblSemJogos.getStyleClass().add("card-text");
            lblSemJogos.setStyle("-fx-font-style: italic; -fx-text-fill: rgba(255, 255, 255, 0.5); -fx-alignment: center; -fx-padding: 20;");
            matchContainer.getChildren().add(lblSemJogos);
            return;
        }
        
        for (PartidaTorneio partida : partidasDoDia) {
            matchContainer.getChildren().add(criarCartaoPartidaDia(partida));
        }
    }

    private HBox criarCartaoPartidaDia(PartidaTorneio partidaTorneio) {
        HBox card = new HBox(15);
        card.getStyleClass().add("group-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        // Fase ou Grupo Label
        String infoFase = "";
        if (partidaTorneio.getFase() == FaseTorneio.FASE_DE_GRUPOS) {
            infoFase = "Grupo " + partidaTorneio.getGrupo().getIdentificador() + " · Rodada " + partidaTorneio.getRodada();
        } else {
            infoFase = formatarFase(partidaTorneio.getFase());
        }
        
        Label lblInfo = new Label(infoFase);
        lblInfo.getStyleClass().add("eyebrow");
        lblInfo.setStyle("-fx-min-width: 180; -fx-text-fill: #8bf0a1;");
        
        boolean definido = (partidaTorneio.getMandante() != null && partidaTorneio.getVisitante() != null);
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        VBox centerContainer = new VBox(5);
        centerContainer.setAlignment(Pos.CENTER);
        
        HBox matchLayout = new HBox(15);
        matchLayout.setAlignment(Pos.CENTER);
        
        Label lblStatus = new Label();
        lblStatus.setStyle("-fx-min-width: 100; -fx-alignment: center;");
        
        if (!definido) {
            // Confronto indefinido (ex: semi-final a definir)
            String textoIndefinido = formatarFase(partidaTorneio.getFase());
            Label lblConfronto = new Label(textoIndefinido);
            lblConfronto.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.4);");
            matchLayout.getChildren().add(lblConfronto);
            
            lblStatus.setText("A definir");
            lblStatus.getStyleClass().add("match-pill");
            lblStatus.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-text-fill: rgba(255, 255, 255, 0.4); -fx-alignment: center; -fx-min-width: 100;");
            
            centerContainer.getChildren().add(matchLayout);
        } else {
            String mandanteNome = nomeComBandeira(partidaTorneio.getMandante());
            String visitanteNome = nomeComBandeira(partidaTorneio.getVisitante());
            
            Label lblMandante = new Label(mandanteNome);
            lblMandante.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fbff; -fx-min-width: 180; -fx-alignment: CENTER_RIGHT;");
            
            Label lblPlacar;
            if (partidaTorneio.getStatus() == StatusPartidaTorneio.CONCLUIDA) {
                Partida partida = partidaTorneio.getPartida();
                String placarTexto = partida.getGolsMandante() + " x " + partida.getGolsVisitante();
                lblPlacar = new Label(placarTexto);
                lblPlacar.getStyleClass().add("status-pill");
                lblPlacar.setStyle("-fx-background-color: rgba(139, 240, 161, 0.15); -fx-text-fill: #8bf0a1; -fx-min-width: 70; -fx-alignment: center;");
                
                lblStatus.setText("Concluído");
                lblStatus.getStyleClass().add("status-pill");
                lblStatus.setStyle("-fx-background-color: rgba(139, 240, 161, 0.1); -fx-text-fill: #8bf0a1; -fx-alignment: center; -fx-min-width: 100;");
            } else if (partidaTorneio.getStatus() == StatusPartidaTorneio.EM_ANDAMENTO) {
                lblPlacar = new Label("vs");
                lblPlacar.getStyleClass().add("match-pill");
                lblPlacar.setStyle("-fx-min-width: 70; -fx-alignment: center;");
                
                lblStatus.setText("AO VIVO");
                lblStatus.getStyleClass().add("status-pill");
                lblStatus.setStyle("-fx-background-color: rgba(255, 107, 107, 0.15); -fx-text-fill: #ff6b6b; -fx-alignment: center; -fx-min-width: 100;");
            } else {
                lblPlacar = new Label("vs");
                lblPlacar.getStyleClass().add("match-pill");
                lblPlacar.setStyle("-fx-min-width: 70; -fx-alignment: center;");
                
                lblStatus.setText("Agendado");
                lblStatus.getStyleClass().add("match-pill");
                lblStatus.setStyle("-fx-alignment: center; -fx-min-width: 100;");
            }
            
            Label lblVisitante = new Label(visitanteNome);
            lblVisitante.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fbff; -fx-min-width: 180; -fx-alignment: CENTER_LEFT;");
            
            matchLayout.getChildren().addAll(lblMandante, lblPlacar, lblVisitante);
            centerContainer.getChildren().add(matchLayout);
            
            if (partidaTorneio.getStatus() == StatusPartidaTorneio.CONCLUIDA) {
                Partida partida = partidaTorneio.getPartida();
                if (partida != null && partida.getGolsMandante() == partida.getGolsVisitante() && partidaTorneio.getVencedor() != null) {
                    int penaltisMandante = 0;
                    int penaltisVisitante = 0;
                    for (EventoPartida ev : partida.getEventos()) {
                        if ("PenaltiConvertidoMandante".equals(ev.getTipo())) {
                            penaltisMandante++;
                        } else if ("PenaltiConvertidoVisitante".equals(ev.getTipo())) {
                            penaltisVisitante++;
                        }
                    }
                    Label lblPenaltis = new Label("Pênaltis: " + penaltisMandante + " x " + penaltisVisitante + " (" + formatarNomePais(partidaTorneio.getVencedor().getNome()) + " venceu)");
                    lblPenaltis.setStyle("-fx-font-size: 11px; -fx-text-fill: #8bf0a1; -fx-font-style: italic;");
                    centerContainer.getChildren().add(lblPenaltis);
                }
            }
        }
        
        card.getChildren().addAll(lblInfo, spacer1, centerContainer, spacer2, lblStatus);
        
        return card;
    }

    private String nomeComBandeira(Time time) {
        if (time == null) return "";
        return obterBandeira(time.getNome()) + " " + formatarNomePais(time.getNome());
    }

    private String obterBandeira(String nomeBruto) {
        if (nomeBruto == null) return "";
        String nome = nomeBruto.trim().toLowerCase();

        if (nome.equals("africa_do_sul")) return "[RSA]";
        if (nome.equals("alemanha")) return "[GER]";
        if (nome.equals("arabia_saudita")) return "[KSA]";
        if (nome.equals("argelia")) return "[ALG]";
        if (nome.equals("argentina")) return "[ARG]";
        if (nome.equals("australia")) return "[AUS]";
        if (nome.equals("austria")) return "[AUT]";
        if (nome.equals("belgica")) return "[BEL]";
        if (nome.equals("bosnia_e_herzegovina")) return "[BIH]";
        if (nome.equals("brasil")) return "[BRA]";
        if (nome.equals("cabo_verde")) return "[CPV]";
        if (nome.equals("camaroes")) return "[CMR]";
        if (nome.equals("canada")) return "[CAN]";
        if (nome.equals("catar")) return "[QAT]";
        if (nome.equals("colombia")) return "[COL]";
        if (nome.equals("coreia_do_sul")) return "[KOR]";
        if (nome.equals("costa_do_marfim")) return "[CIV]";
        if (nome.equals("croacia")) return "[CRO]";
        if (nome.equals("curacau")) return "[CUW]";
        if (nome.equals("dinamarca")) return "[DEN]";
        if (nome.equals("egito")) return "[EGY]";
        if (nome.equals("equador")) return "[ECU]";
        if (nome.equals("escocia")) return "[SCO]";
        if (nome.equals("espanha")) return "[ESP]";
        if (nome.equals("estados_unidos")) return "[USA]";
        if (nome.equals("franca")) return "[FRA]";
        if (nome.equals("gana")) return "[GHA]";
        if (nome.equals("haiti")) return "[HAI]";
        if (nome.equals("holanda")) return "[NED]";
        if (nome.equals("inglaterra")) return "[ENG]";
        if (nome.equals("ira")) return "[IRN]";
        if (nome.equals("iraque")) return "[IRQ]";
        if (nome.equals("japao")) return "[JPN]";
        if (nome.equals("jordania")) return "[JOR]";
        if (nome.equals("marrocos")) return "[MAR]";
        if (nome.equals("mexico")) return "[MEX]";
        if (nome.equals("noruega")) return "[NOR]";
        if (nome.equals("nova_zelandia")) return "[NZL]";
        if (nome.equals("panama")) return "[PAN]";
        if (nome.equals("paraguai")) return "[PAR]";
        if (nome.equals("portugal")) return "[POR]";
        if (nome.equals("republica_democratica_do_congo")) return "[COD]";
        if (nome.equals("republica_tcheca")) return "[CZE]";
        if (nome.equals("senegal")) return "[SEN]";
        if (nome.equals("suecia")) return "[SWE]";
        if (nome.equals("suica")) return "[SUI]";
        if (nome.equals("tunisia")) return "[TUN]";
        if (nome.equals("turquia")) return "[TUR]";
        if (nome.equals("uruguai")) return "[URU]";
        if (nome.equals("uzbequistao")) return "[UZB]";

        return "[INT]";
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
        return "Final";
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

    @Override
    public Parent getRoot() {
        return root;
    }
}
