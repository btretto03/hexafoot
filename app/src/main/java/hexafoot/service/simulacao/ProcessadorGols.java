package hexafoot.service.simulacao;

import hexafoot.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Sorteia gols para cada equipe a cada minuto. O limiar da rolagem de 1 a 1.000 é
 * {@code CHANCE_GOL * ataque² / (ataque² + defesa²)}, favorecendo diferenças de força.
 */
public class ProcessadorGols implements ObserverMinuto {

    private Random random;
    private SorteadorJogador sorteador;

    public ProcessadorGols() {
        this.random = new Random();
        this.sorteador = new SorteadorJogador();
    }

    /**
     * Realiza uma tentativa independente para cada equipe no minuto informado.
     */
    @Override
    public void atualizarMinuto(int minutoAtual, Partida partida) {
        tentarGol(minutoAtual, partida, partida.getMandante(), partida.getVisitante()); // Mandante atacando
        tentarGol(minutoAtual, partida, partida.getVisitante(), partida.getMandante()); // Visitante atacando
    }

    //-----------------Lógica de gol-----------------
    /**
     * Em caso de sucesso, incrementa o placar, sorteia o autor entre os titulares
     * ativos que não são goleiros e registra o evento correspondente ao lado atacante.
     */
    private void tentarGol(int minutoAtual, Partida partida, Time atacante, Time defensor) {
        int forcaAtaque = atacante.calcularForcaAtaqueAtual();
        int forcaDefesa = defensor.calcularForcaDefesaAtual();

        int chanceBase = RegrasSimulacao.CHANCE_GOL.getValor();

        //usamos o quadrado das forcas para a diferenca de qualidade entre os times pesar mais no resultado
        //(so a proporcao direta deixava times bem mais fortes com uma chance quase igual a de times fracos)
        int pesoAtaque = forcaAtaque * forcaAtaque;
        int pesoDefesa = forcaDefesa * forcaDefesa;
        int chanceFinal = (chanceBase * pesoAtaque) / (pesoAtaque + pesoDefesa);

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

    /**
     * @return nova lista somente com jogadores ativos que não sejam goleiros
     */
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
