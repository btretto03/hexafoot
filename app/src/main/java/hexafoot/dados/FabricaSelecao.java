package hexafoot.dados;

import hexafoot.model.Jogador;
import hexafoot.model.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * Converte os CSVs de elencos em jogadores e seleções do jogo.
 */
public class FabricaSelecao {

    private LeitorCSVSelecao leitor;

    public FabricaSelecao() {
        this.leitor = new LeitorCSVSelecao();
    }

    //-----------------Criação de jogadores e times-----------------

    /**
     * Converte uma linha de elenco em jogador.
     *
     * @param campos vetor no formato
     *               {@code [nome, clube, posição, ataque, defesa, resistência física, estresse]}
     * @throws NumberFormatException se um atributo numérico não for inteiro
     */
    public Jogador instanciarJogador(String[] campos) {
        String nome = campos[0];
        String posicao = campos[2];
        int ataque = Integer.parseInt(campos[3]);
        int defesa = Integer.parseInt(campos[4]);
        int fisico = Integer.parseInt(campos[5]);
        int estresse = Integer.parseInt(campos[6]);

        return new Jogador(nome, posicao, ataque, defesa, fisico, estresse);
    }

    /**
     * Cria uma seleção e redistribui todo o elenco entre titulares e reservas segundo
     * a formação padrão e a qualidade por posição.
     */
    public Time montarTime(String nomePais, List<Jogador> jogadores) {
        Time time = new Time(nomePais);
        for (int i = 0; i < jogadores.size(); i ++) {
            if (i < 11) {
                time.adicionarTitular(jogadores.get(i));
            } else {
                time.adicionarReserva(jogadores.get(i));
            }
        }
        time.setFormacaoAtual(hexafoot.model.Formacao.F_4_3_3);
        time.escalarMelhoresJogadores();
        return time;
    }

    
    /**
     * Carrega todos os jogadores do primeiro arquivo identificado como Brasil, sem
     * validar o tamanho do elenco.
     *
     * @return jogadores em ordem de leitura, ou lista vazia se o arquivo não existir
     */
    public List<Jogador> processarListaBrasil() {
        List<ArquivoCSVSelecao> arquivos = leitor.listarArquivosDeSelecoes();
        List<Jogador> jogadoresBrasil = new ArrayList<>();

        for (ArquivoCSVSelecao arquivo : arquivos) {
            if (arquivo.isBrasil()) {
                for (String[] campos : arquivo.getLinhas()) {
                    Jogador jogador = instanciarJogador(campos);
                    jogadoresBrasil.add(jogador);
                }
                break;
            }
        }

        return jogadoresBrasil;
    }


    /**
     * Monta uma seleção para cada arquivo internacional, sem validar a quantidade de
     * jogadores de cada elenco. O Brasil é excluído para ser tratado pela convocação.
     */
    public List<Time> processarListasInternacionais() {
        List<ArquivoCSVSelecao> arquivos = leitor.listarArquivosDeSelecoes();
        List<Time> selecoes = new ArrayList<>();

        for (ArquivoCSVSelecao arquivo : arquivos) {
            if (arquivo.isBrasil()) {
                continue; //brasil é tratado separado pela convocação
            }

            List<Jogador> jogadores = new ArrayList<>();
            for (String[] campos : arquivo.getLinhas()) {
                Jogador jogador = instanciarJogador(campos);
                jogadores.add(jogador);
            }

            Time time = montarTime(arquivo.getNomePais(), jogadores);
            selecoes.add(time);
        }

        return selecoes;
    }
}
