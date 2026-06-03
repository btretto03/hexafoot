package hexafoot.service.simulacao;

import hexafoot.model.Jogador;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Entidade SorteadorJogador - Ferramenta que é responsável por selecionar jogadores
 * em listas, aplicando pesos baseados em seus atributos.
 */

public class SorteadorJogador {
    private Random random;

    public SorteadorJogador() {
        this.random = new Random();
    }

    // Sorteio por ataque jogadores com mais ataque tem mais chance de ser escolhido
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

    //Sorteio para cartoes/lesao (mesma lógica do ataque)
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