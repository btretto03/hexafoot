package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Time;

import java.util.List;

public class GerenciadorConvocacao {
    private List<Jogador> jogadoresDisponiveis;
    private Time elencoOficial;

    public GerenciadorConvocacao(List<Jogador> jogadoresBrasileiros) {
        this.jogadoresDisponiveis = jogadoresBrasileiros;
        this.elencoOficial = new Time("Brasil");
    }

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