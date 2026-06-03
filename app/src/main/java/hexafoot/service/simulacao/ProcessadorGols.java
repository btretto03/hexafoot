package hexafoot.service.simulacao;

import hexafoot.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Entidade ProcessadorGols - Responsável por calcular as chances de gol a cada minuto
 * usando rolagem de dados (inteiros) e atualizar o placar.
 */
public class ProcessadorGols implements ObserverMinuto {

    private Random random;
    private SorteadorJogador sorteador;

    public ProcessadorGols() {
        this.random = new Random();
        this.sorteador = new SorteadorJogador();
    }

    @Override
    public void atualizarMinuto(int minutoAtual, Partida partida) {
        tentarGol(minutoAtual, partida, partida.getMandante(), partida.getVisitante()); // Mandante atacando
        tentarGol(minutoAtual, partida, partida.getVisitante(), partida.getMandante()); // Visitante atacando
    }

    //-----------------Lógica de gol-----------------
    private void tentarGol(int minutoAtual, Partida partida, Time atacante, Time defensor) {
        int forcaAtaque = atacante.calcularForcaAtaqueAtual();
        int forcaDefesa = defensor.calcularForcaDefesaAtual();

        int chanceBase = RegrasSimulacao.chanceBaseGol;
        int chanceFinal = (chanceBase * forcaAtaque) / (forcaAtaque + forcaDefesa);

        int rolagemGol = random.nextInt(1000) + 1;

        if (rolagemGol <= chanceFinal) {
            
            List<Jogador> candidatos = filtrarNaoGoleiros(atacante.getTitulares());
            Jogador autor = sorteador.sortearPorAtaque(candidatos);
            
            // Verifica de quem foi o gol comparando os objetos diretamente
            if (atacante == partida.getMandante()) {
                partida.adicionarGolMandante();
                partida.adicionarEvento(new EventoPartida(minutoAtual, "GolMandante", autor));
            } else {
                partida.adicionarGolVisitante();
                partida.adicionarEvento(new EventoPartida(minutoAtual, "GolVisitante", autor));
            }
        }
    }

    private List<Jogador> filtrarNaoGoleiros(List<Jogador> jogadores) { //para goleiros nao marcar gols
        List<Jogador> candidatos = new ArrayList<>();
        
        for (Jogador jogador : jogadores) {
            if ("Ativo".equals(jogador.getStatus()) == false) {
                continue;
            }
            if ("Goleiro".equals(jogador.getPosicao()) == false) {
                candidatos.add(jogador);
            }
        }
        return candidatos;
    }
}