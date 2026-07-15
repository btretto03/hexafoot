package hexafoot.ui;

import hexafoot.dados.FabricaSelecao;
import hexafoot.model.Jogador;
import hexafoot.model.Time;
import hexafoot.service.simulacao.GerenciadorConvocacao;
import hexafoot.service.torneio.GerenciadorTorneio;

import java.util.List;

/**
 * Mantém o estado de campanha compartilhado entre as telas e suas etapas de inicialização.
 */
public class GameSession {
    private final FabricaSelecao fabricaSelecao;
    private GerenciadorConvocacao gerenciadorConvocacao;
    private GerenciadorTorneio gerenciadorTorneio;
    private List<Time> selecoesInternacionais;

    public GameSession() {
        this.fabricaSelecao = new FabricaSelecao();
    }

    /**
     * Reinicia a convocação e invalida torneio e seleções carregados da campanha anterior.
     */
    public void iniciarNovoJogo() {
        this.gerenciadorConvocacao = new GerenciadorConvocacao(fabricaSelecao.processarListaBrasil());
        this.gerenciadorTorneio = null;
        this.selecoesInternacionais = null;
    }

    /**
     * Obtém a convocação corrente, iniciando uma nova campanha se ela ainda não existir.
     *
     * @return gerenciador de convocação ativo
     */
    public GerenciadorConvocacao getGerenciadorConvocacao() {
        if (gerenciadorConvocacao == null) {
            iniciarNovoJogo();
        }
        return gerenciadorConvocacao;
    }

    public List<Jogador> getJogadoresDisponiveisBrasil() {
        return getGerenciadorConvocacao().getJogadoresDisponiveis();
    }

    /**
     * Obtém o Brasil do torneio já iniciado ou, antes disso, o elenco em convocação.
     *
     * @return elenco brasileiro correspondente à etapa atual da campanha
     */
    public Time getElencoBrasil() {
        if (gerenciadorTorneio != null) {
            return gerenciadorTorneio.getBrasil();
        }
        return getGerenciadorConvocacao().getElencoOficial();
    }

    /**
     * Cria um torneio com o elenco convocado e as seleções internacionais carregadas.
     */
    public void iniciarTorneio() {
        this.gerenciadorTorneio = new GerenciadorTorneio(getElencoBrasil(), getSelecoesInternacionais());
    }

    /**
     * Substitui o torneio corrente pelo estado desserializado de uma campanha.
     *
     * @param gerenciadorTorneioSalvo estado completo recuperado do salvamento
     */
    public void carregarTorneio(GerenciadorTorneio gerenciadorTorneioSalvo) {
        this.gerenciadorTorneio = gerenciadorTorneioSalvo;
    }

    public GerenciadorTorneio getGerenciadorTorneio() {
        return gerenciadorTorneio;
    }

    /**
     * Carrega as seleções internacionais sob demanda e reutiliza a mesma lista na campanha.
     *
     * @return seleções que participarão do torneio
     */
    public List<Time> getSelecoesInternacionais() {
        if (selecoesInternacionais == null) {
            selecoesInternacionais = fabricaSelecao.processarListasInternacionais();
        }

        return selecoesInternacionais;
    }
}
