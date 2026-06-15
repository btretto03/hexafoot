package hexafoot.dados;

import hexafoot.model.Jogador;
import hexafoot.model.Time;

import java.util.ArrayList;
import java.util.List;

/**
 *Entidade responsável por ler os arquivos csv das seleções e montar os objetos Jogador e Time para o jogo.
 */
public class FabricaSelecao {

    private LeitorCSVSelecao leitor;

    public FabricaSelecao() {
        this.leitor = new LeitorCSVSelecao();
    }

    //-----------------Criação de jogadores e times-----------------

    public Jogador instanciarJogador(String[] campos) {
        String nome = campos[0];
        String posicao = campos[2];
        int ataque = Integer.parseInt(campos[3]);
        int defesa = Integer.parseInt(campos[4]);
        int fisico = Integer.parseInt(campos[5]);
        int estresse = Integer.parseInt(campos[6]);

        return new Jogador(nome, posicao, ataque, defesa, fisico, estresse);
    }

    public Time montarTime(String nomePais, List<Jogador> jogadores) {
        Time time = new Time(nomePais);
        for (int i = 0; i < jogadores.size(); i ++) {
            if (i < 11) {
                time.adicionarTitular(jogadores.get(i));
            } else {
                time.adicionarReserva(jogadores.get(i));
            }
        }

        return time;
    }

    
    public List<Jogador> processarListaBrasil() { //processamento do brasil (precisa ser 50 jogadores)
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


    public List<Time> processarListasInternacionais() { //processa as outras seleções (precisa ser 26 jogadores cada)
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
