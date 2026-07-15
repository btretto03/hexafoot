package hexafoot.service.simulacao;

import hexafoot.model.*;

import java.util.List;
import java.util.Random;

/**
 * Sorteia lesões por titular ativo e por minuto. A chance usa uma rolagem de
 * 1 a 10.000 e aumenta em um ponto a cada cinco pontos de fadiga.
 */
public class ProcessadorLesoes implements ObserverMinuto {
    private Random random;
    private SorteadorJogador sorteador;
    private Time timeSemAutoSubstituicao; //time do jogador humano: aqui quem escolhe o substituto e o proprio jogador

    public ProcessadorLesoes() {
        this(null);
    }

    /**
     * @param timeSemAutoSubstituicao equipe controlada pelo usuário, mantida sem
     *                               substituição automática; {@code null} habilita
     *                               o automatismo para ambas as equipes
     */
    public ProcessadorLesoes(Time timeSemAutoSubstituicao) {
        this.random = new Random();
        this.sorteador = new SorteadorJogador();
        this.timeSemAutoSubstituicao = timeSemAutoSubstituicao;
    }

    /**
     * Processa lesões de mandante e visitante no minuto informado.
     */
    @Override
    public void atualizarMinuto(int minutoAtual, Partida partida) {
        processarLesaoParaTime(partida.getMandante(), partida, minutoAtual);
        processarLesaoParaTime(partida.getVisitante(), partida, minutoAtual);
    }

    /**
     * Sorteia afastamento entre os limites inclusivos das regras e registra a lesão.
     * Para equipes automáticas, tenta substituir por uma reserva ativa sorteada pelo
     * ataque; sem substituição disponível, remove o lesionado dos titulares. Na equipe
     * do usuário, o lesionado permanece entre os titulares até a escolha manual.
     */
    private void processarLesaoParaTime(Time time, Partida partida, int minutoAtual) {
        
        for (int i = 0; i < time.getTitulares().size(); i ++) {
            Jogador jogador = time.getTitulares().get(i);

            if ("Ativo".equals(jogador.getStatus()) == false) {
                continue;
            }

            int chanceLesao = RegrasSimulacao.CHANCE_BASE_LESAO.getValor(); 
            int fadiga = 100 - jogador.getFisico(); //quanto mais fadiga maior chance de lesao
            chanceLesao = chanceLesao + (fadiga / 5); 

            int sorteioLesao = random.nextInt(10000) + 1;

            if (sorteioLesao <= chanceLesao) {
            
                int afastamentoMin = RegrasSimulacao.AFASTAMENTO_MIN_LESAO.getValor(); //também podemos ajustar esses
                int afastamentoMax = RegrasSimulacao.AFASTAMENTO_MAX_LESAO.getValor();

                int faixa = afastamentoMax - afastamentoMin + 1;
                int afastamento = afastamentoMin + random.nextInt(faixa);
                
                jogador.sofrerLesao(afastamento);
                partida.adicionarEvento(new EventoPartida(minutoAtual, "Lesao", jogador));

                if (time == timeSemAutoSubstituicao) { //time do jogador: o lesionado fica na lista ate o proprio jogador escolher o substituto
                    continue;
                }

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
