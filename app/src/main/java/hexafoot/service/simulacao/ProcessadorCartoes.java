package hexafoot.service.simulacao;

import hexafoot.model.*;
import java.util.Random;

/**
 * Entidade ProcessadorCartoes - Responsável por sortear eventos de cartão.
 */
public class ProcessadorCartoes implements ObserverMinuto {
    private Random random;

    public ProcessadorCartoes() {
        this.random = new Random();
    }

    @Override
    public void atualizarMinuto(int minutoAtual, Partida partida) {
        processarCartaoParaTime(partida.getMandante(), partida, minutoAtual);
        processarCartaoParaTime(partida.getVisitante(), partida, minutoAtual);
    }

    private void processarCartaoParaTime(Time time, Partida partida, int minutoAtual) {
        
        for (int i = 0; i < time.getTitulares().size(); i ++) {
            Jogador jogador = time.getTitulares().get(i);

            if ("Ativo".equals(jogador.getStatus()) == false) { //pega só os titulares ativos
                continue;
            }

            int chanceCartao = RegrasSimulacao.CHANCE_BASE_FALTA; //esses valores precisamos testar com o jogo rodando para ajustar
            int rolagemFalta = random.nextInt(1000) + 1;

            if (rolagemFalta <= chanceCartao) {
                int chanceVermelho = RegrasSimulacao.CHANCE_BASE_VERMELHO; //testar
                chanceVermelho = chanceVermelho + (jogador.getEstresse() / 10); //atributo estresse alto aumenta chance de vermelho
                
                int rolagemCor = random.nextInt(100) + 1; 

                if (rolagemCor <= chanceVermelho) { //vermelho direto
                    jogador.aplicarCartaoVermelho();
                    time.getTitulares().remove(i); 
                    i --; 
                    partida.adicionarEvento(new EventoPartida(minutoAtual, "CartaoVermelho", jogador));
                } else {
                    jogador.aplicarCartaoAmarelo();
                    partida.adicionarEvento(new EventoPartida(minutoAtual, "CartaoAmarelo", jogador));

                    // 2 amarelos é expulso
                    if ("Ativo".equals(jogador.getStatus()) == false) {
                        time.getTitulares().remove(i);
                        i --; 
                        partida.adicionarEvento(new EventoPartida(minutoAtual, "SegundoAmarelo", jogador));
                    }
                }
            }
        }
    }
}