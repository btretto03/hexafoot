package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Time;

import java.util.List;

/**
 * Monta a convocação do Brasil a partir de uma lista mutável de jogadores disponíveis.
 */
public class GerenciadorConvocacao {
    private List<Jogador> jogadoresDisponiveis;
    private Time elencoOficial;

    /**
     * Mantém a própria lista recebida como fonte de disponíveis; inclusões e remoções do
     * elenco também alteram essa lista.
     *
     * @param jogadoresBrasileiros lista mutável de candidatos à convocação
     */
    public GerenciadorConvocacao(List<Jogador> jogadoresBrasileiros) {
        this.jogadoresDisponiveis = jogadoresBrasileiros;
        this.elencoOficial = new Time("Brasil");
    }

    /**
     * Transfere um jogador disponível para o elenco, até o limite de 26 atletas.
     * Os 11 primeiros são titulares e os demais, reservas. Não altera o estado quando
     * o jogador não está disponível ou o elenco já atingiu o limite.
     */
    public void inserirNoElenco(Jogador jogador) {
        if (jogadoresDisponiveis.contains(jogador) == true) {
            int totalNoElenco = elencoOficial.getTitulares().size() + elencoOficial.getReservas().size();
            
            if (totalNoElenco < 26) { //limite oficial da copa
                jogadoresDisponiveis.remove(jogador);
                
                if (elencoOficial.getTitulares().size() < 11) { //coloca os 11 primeiros como titulares
                    elencoOficial.adicionarTitular(jogador);
                } else { //o restante vai pro banco
                    elencoOficial.adicionarReserva(jogador);
                }
            }
        }
    }

    /**
     * Retira o jogador dos titulares ou reservas e o devolve à lista de disponíveis.
     * Não altera o estado caso ele não pertença ao elenco.
     */
    public void removerDoElenco(Jogador jogador) {
        boolean eraTitular = elencoOficial.removerTitular(jogador);
        boolean eraReserva = false;
        
        if (eraTitular == false) { //se nao era titular tenta remover dos reservas
            eraReserva = elencoOficial.removerReserva(jogador);
        }
        
        if (eraTitular == true || eraReserva == true) { //se foi removido do time volta pra lista de disponiveis
            jogadoresDisponiveis.add(jogador);
        }
    }

    /**
     * @return {@code true} somente quando o elenco possui exatamente 26 atletas
     */
    public boolean validarTamanhoConvocacao() {
        int totalNoElenco = elencoOficial.getTitulares().size() + elencoOficial.getReservas().size();
        return totalNoElenco == 26; //exige exatamente 26 atletas
    }

    public List<Jogador> getJogadoresDisponiveis() {
        return jogadoresDisponiveis;
    }

    public Time getElencoOficial() {
        return elencoOficial;
    }
}
