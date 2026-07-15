package hexafoot.model;
import hexafoot.model.strategy.EstrategiaSimulacao;
import hexafoot.model.strategy.TaticaEquilibrada;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Time - Representa cada seleção do torneio, com atributos como nome, titulares e reservas, tática atual e estatísticas da tabela.
 */
public class Time implements Serializable {
    private String nome;
    private List<Jogador> titulares;
    private List<Jogador> reservas;
    private EstrategiaSimulacao taticaAtual;
    private Formacao formacaoAtual;
    private int pontos;
    private int golsMarcados;
    private int golsSofridos;
    private int vitorias;
    private int derrotas;
    private int empates;
    

    public Time(String nome) {
        this.nome = nome;
        this.titulares = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.taticaAtual = new TaticaEquilibrada(); //tática padrão
        this.formacaoAtual = Formacao.F_4_3_3; //formação padrão
        this.pontos = 0;
        this.golsMarcados = 0;
        this.golsSofridos = 0;
        this.vitorias = 0;
        this.derrotas = 0;
        this.empates = 0;
    }

//-----------------Gerenciamento de elenco-----------------
    public void adicionarTitular(Jogador jogador) {
        if (titulares.size() < 11 && titulares.contains(jogador) == false && reservas.contains(jogador) == false) {
            titulares.add(jogador);
        }
    }

    public void adicionarReserva(Jogador jogador) {
        if (titulares.contains(jogador) == false && reservas.contains(jogador) == false) {
            reservas.add(jogador);
        }
    }

    public boolean removerTitular(Jogador jogador) {
        return titulares.remove(jogador);
    }

    public boolean removerReserva(Jogador jogador) {
        return reservas.remove(jogador);
    }

    public int calcularForcaAtaqueAtual() {
        double soma = 0;
        for (Jogador jogador : titulares) {
            if ("Ativo".equals(jogador.getStatus())) {
                double fatorFisico = 0.5 + 0.5 * (jogador.getFisico() / 100.0); //o fisico afeta o desempenho
                soma += jogador.getAtaque() * fatorFisico;
            }

        }
        soma = soma * taticaAtual.getModificadorAtaque() * formacaoAtual.getModificadorAtaque();
        return (int) Math.round(soma);
    }

    public int calcularForcaDefesaAtual() {
        double soma = 0;
        for (Jogador jogador : titulares) {
            if ("Ativo".equals(jogador.getStatus())) {
                double fatorFisico = 0.5 + 0.5 * (jogador.getFisico() / 100.0);
                soma += jogador.getDefesa() * fatorFisico;
            }
        }

        soma = soma * taticaAtual.getModificadorDefesa() * formacaoAtual.getModificadorDefesa();
        return (int) Math.round(soma);
    }

//-----------------Atualização da tabela e estatísticas-----------------
    public int getSaldoGols() {
        return this.golsMarcados - this.golsSofridos;
    }

    public void registrarVitoria() {
        this.vitorias++;
        this.pontos += 3;
    }

    public void registrarEmpate() {
        this.empates++;
        this.pontos += 1;
    }

    public void registrarDerrota() {
        this.derrotas++;
    }

//-----------------Cálculo de força do time (titulares)-----------------
    public int getAtaqueTitulares() {
        int soma = 0;

        for (Jogador jogador : titulares) {
            soma += jogador.getAtaque();
        }
        return soma;
    }

    public int getDefesaTitulares() {
        int soma = 0;
        for (Jogador jogador : titulares) {
            soma += jogador.getDefesa();
        }
        return soma;
    }

    public double getAtaqueEfetivo() {
        return getAtaqueTitulares() * this.taticaAtual.getModificadorAtaque();
    }

    public double getDefesaEfetiva() {
        return getDefesaTitulares() * this.taticaAtual.getModificadorDefesa();
    }

//-----------------getters e setters-----------------
    public String getNome() {
        return nome;
    }

    public List<Jogador> getTitulares() {
        return titulares;
    }

    public List<Jogador> getReservas() {
        return reservas;
    }

    public EstrategiaSimulacao getTaticaAtual() {
        return taticaAtual;
    }

    public int getPontos() {
        return pontos;
    }

    public int getGolsMarcados() {
        return golsMarcados;
    }

    public int getGolsSofridos() {
        return golsSofridos;
    }

    public int getVitorias() {
        return vitorias;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public int getEmpates() {
        return empates;
    }

    public void setTaticaAtual(EstrategiaSimulacao taticaAtual) {
        this.taticaAtual = taticaAtual;
    }

    public void setGolsMarcados(int golsMarcados) {
        this.golsMarcados = golsMarcados;
    }

    public void setGolsSofridos(int golsSofridos) {
        this.golsSofridos = golsSofridos;
    }
    
    public Formacao getFormacaoAtual() {
        return formacaoAtual;
    }

    public void setFormacaoAtual(Formacao formacaoAtual) {
        this.formacaoAtual = formacaoAtual;
    }

    public void escalarMelhoresJogadores() {
        List<Jogador> todos = new ArrayList<>();
        todos.addAll(titulares);
        todos.addAll(reservas);

        List<Jogador> goleiros = new ArrayList<>();
        List<Jogador> defensores = new ArrayList<>();
        List<Jogador> meias = new ArrayList<>();
        List<Jogador> atacantes = new ArrayList<>();

        for (Jogador j : todos) {
            String pos = normalizarPosicao(j.getPosicao());
            if ("goleiro".equals(pos)) {
                goleiros.add(j);
            } else if ("defensor".equals(pos)) {
                defensores.add(j);
            } else if ("meio-campista".equals(pos)) {
                meias.add(j);
            } else {
                atacantes.add(j);
            }
        }

        // Ordenar por qualidade (decrescente)
        goleiros.sort((j1, j2) -> Integer.compare(j2.getDefesa(), j1.getDefesa()));
        defensores.sort((j1, j2) -> Integer.compare(j2.getDefesa(), j1.getDefesa()));
        meias.sort((j1, j2) -> Integer.compare((j2.getAtaque() + j2.getDefesa()), (j1.getAtaque() + j1.getDefesa())));
        atacantes.sort((j1, j2) -> Integer.compare(j2.getAtaque(), j1.getAtaque()));

        int qtdG = 1;
        int qtdD = formacaoAtual.getQuantidadeDefensores();
        int qtdM = formacaoAtual.getQuantidadeMeio();
        int qtdA = formacaoAtual.getQuantidadeAtacantes();

        List<Jogador> novosTitulares = new ArrayList<>();
        List<Jogador> novosReservas = new ArrayList<>();

        preencherMelhores(goleiros, qtdG, novosTitulares, novosReservas);
        preencherMelhores(defensores, qtdD, novosTitulares, novosReservas);
        preencherMelhores(meias, qtdM, novosTitulares, novosReservas);
        preencherMelhores(atacantes, qtdA, novosTitulares, novosReservas);

        // Prevenção contra escassez de alguma posição específica
        while (novosTitulares.size() < 11 && !novosReservas.isEmpty()) {
            novosTitulares.add(novosReservas.remove(0));
        }

        titulares.clear();
        titulares.addAll(novosTitulares);

        reservas.clear();
        reservas.addAll(novosReservas);
    }

    private void preencherMelhores(List<Jogador> origem, int qtd, List<Jogador> targetTitulares, List<Jogador> targetReservas) {
        for (int i = 0; i < origem.size(); i++) {
            if (i < qtd) {
                targetTitulares.add(origem.get(i));
            } else {
                targetReservas.add(origem.get(i));
            }
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
}