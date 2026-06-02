package hexafoot.model;
import hexafoot.model.strategy.EstrategiaSimulacao;
import hexafoot.model.strategy.TaticaEquilibrada;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Time - Representa cada seleção do torneio, com atributos como nome, titulares e reservas, tática atual e estatísticas da tabela.
 */
public class Time {
    private String nome;
    private List<Jogador> titulares;
    private List<Jogador> reservas;
    private EstrategiaSimulacao taticaAtual;
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
        this.pontos = 0;
        this.golsMarcados = 0;
        this.golsSofridos = 0;
        this.vitorias = 0;
        this.derrotas = 0;
        this.empates = 0;
    }

//-----------------Gerenciamento de elenco-----------------
    public void adicionarTitular(Jogador jogador) {
        if (titulares.size() < 11) {
            titulares.add(jogador);
        }
    }

    public void adicionarReserva(Jogador jogador) {
            reservas.add(jogador);
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
}
