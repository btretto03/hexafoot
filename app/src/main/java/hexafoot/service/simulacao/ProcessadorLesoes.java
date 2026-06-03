package hexafoot.service.simulacao;

import hexafoot.model.*;

import java.util.List;
import java.util.Random;

/**
 * Entidade ProcessadorLesoes - Responsável por sortear lesões.
 */
public class ProcessadorLesoes implements ObserverMinuto {
    private Random random;
    private SorteadorJogador sorteador;

    public ProcessadorLesoes() {
        this.random = new Random();
        this.sorteador = new SorteadorJogador();
    }

    @Override
    public void atualizarMinuto(int minutoAtual, Partida partida) {
        processarLesaoParaTime(partida.getMandante(), partida, minutoAtual);
        processarLesaoParaTime(partida.getVisitante(), partida, minutoAtual);
    }

    private void processarLesaoParaTime(Time time, Partida partida, int minutoAtual) {
        
        for (int i = 0; i < time.getTitulares().size(); i ++) {
            Jogador jogador = time.getTitulares().get(i);

            if ("Ativo".equals(jogador.getStatus()) == false) {
                continue;
            }

            int chanceLesao = RegrasSimulacao.CHANCE_BASE_LESAO; 
            int fadiga = 100 - jogador.getFisico(); //quanto mais fadiga maior chance de lesao
            chanceLesao = chanceLesao + fadiga; 

            int sorteioLesao = random.nextInt(10000) + 1;

            if (sorteioLesao <= chanceLesao) {
            
                int afastamentoMin = RegrasSimulacao.AFASTAMENTO_MIN_LESAO; //também podemos ajustar esses
                int afastamentoMax = RegrasSimulacao.AFASTAMENTO_MAX_LESAO;

                int faixa = afastamentoMax - afastamentoMin + 1;
                int afastamento = afastamentoMin + random.nextInt(faixa);
                
                jogador.sofrerLesao(afastamento);
                partida.adicionarEvento(new EventoPartida(minutoAtual, "Lesao", jogador));

                if (time == partida.getMandante()) { // Lógica de substituição ou saída definitiva
                    List<Jogador> reservas = partida.getReservasDisponiveisMandante();
                    
                    if (partida.mandantePodeSubstituir() && reservas.isEmpty() == false) {
                        Jogador substituto = sorteador.sortearPorAtaque(reservas);
                        partida.substituirMandante(jogador, substituto);
                        partida.adicionarEvento(new EventoPartida(minutoAtual, "Substituicao", jogador, substituto));
                    } else {
                        time.getTitulares().remove(i);  // Não pode substituir, time fica com 10
                        i --;
                    }
                    
                } else {
                    List<Jogador> reservas = partida.getReservasDisponiveisVisitante();
                    if (partida.visitantePodeSubstituir() && reservas.isEmpty() == false) {
                        Jogador substituto = sorteador.sortearPorAtaque(reservas);
                        partida.substituirVisitante(jogador, substituto);
                        partida.adicionarEvento(new EventoPartida(minutoAtual, "Substituicao", jogador, substituto));
                    } else {
                        time.getTitulares().remove(i); // Não pode substituir: Tira o jogador lesionado do campo (equipa fica com 10)
                        i--;
                    }
                }
            }
        }
    }
}