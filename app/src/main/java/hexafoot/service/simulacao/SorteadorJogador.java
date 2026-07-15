package hexafoot.service.simulacao;

import hexafoot.model.Jogador;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seleciona jogadores aleatoriamente com pesos derivados de seus atributos.
 */

public class SorteadorJogador implements Serializable {
    private Random random;

    public SorteadorJogador() {
        this.random = new Random();
    }

    /**
     * Sorteia com probabilidade proporcional ao ataque de cada jogador.
     *
     * @return um dos candidatos com peso de ataque positivo
     * @throws IllegalArgumentException se nenhum candidato tiver ataque positivo
     */
    public Jogador sortearPorAtaque(List<Jogador> jogadores) {
        List<Jogador> lista = new ArrayList<>();

        for (Jogador i : jogadores) { // percorre todos do campo
            for (int j = 0; j < i.getAtaque(); j ++) { //faz com que jogadores com mais ataque tenham mais chances de serem sorteados (terem feito o gol)
                lista.add(i);
            }
        }

        int alvo = random.nextInt(lista.size()); //sorteia o jogador
        return lista.get(alvo);
    }

    /**
     * Sorteia com peso {@code estresse + (100 - físico)}, combinando tensão e fadiga.
     *
     * @return um dos candidatos com peso positivo
     * @throws IllegalArgumentException se nenhum candidato tiver peso positivo
     */
    public Jogador sortearPorEstresse(List<Jogador> jogadores) {
        List<Jogador> lista = new ArrayList<>();

        for (Jogador j : jogadores) {
            int fadiga = 100 - j.getFisico();
            int peso = j.getEstresse() + fadiga;

            for (int i = 0; i < peso; i ++) {
                lista.add(j);
            }
        }
        int alvo = random.nextInt(lista.size());
        return lista.get(alvo);
    }
}
