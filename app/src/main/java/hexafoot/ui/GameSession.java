package hexafoot.ui;

import hexafoot.dados.FabricaSelecao;
import hexafoot.model.Jogador;
import hexafoot.model.Time;
import hexafoot.service.simulacao.GerenciadorConvocacao;
import hexafoot.service.torneio.GerenciadorTorneio;

import java.util.List;

public class GameSession {
    private final FabricaSelecao fabricaSelecao;
    private GerenciadorConvocacao gerenciadorConvocacao;
    private GerenciadorTorneio gerenciadorTorneio;
    private List<Time> selecoesInternacionais;

    public GameSession() {
        this.fabricaSelecao = new FabricaSelecao();
    }

    public void iniciarNovoJogo() {
        this.gerenciadorConvocacao = new GerenciadorConvocacao(fabricaSelecao.processarListaBrasil());
        this.gerenciadorTorneio = null;
        this.selecoesInternacionais = null;
    }

    public GerenciadorConvocacao getGerenciadorConvocacao() {
        if (gerenciadorConvocacao == null) {
            iniciarNovoJogo();
        }
        return gerenciadorConvocacao;
    }

    public List<Jogador> getJogadoresDisponiveisBrasil() {
        return getGerenciadorConvocacao().getJogadoresDisponiveis();
    }

    public Time getElencoBrasil() {
        return getGerenciadorConvocacao().getElencoOficial();
    }

    public void iniciarTorneio() {
        this.gerenciadorTorneio = new GerenciadorTorneio(getElencoBrasil(), getSelecoesInternacionais());
    }

    public GerenciadorTorneio getGerenciadorTorneio() {
        return gerenciadorTorneio;
    }

    public List<Time> getSelecoesInternacionais() {
        if (selecoesInternacionais == null) {
            selecoesInternacionais = fabricaSelecao.processarListasInternacionais();
        }

        return selecoesInternacionais;
    }
}