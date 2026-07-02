package hexafoot.service.simulacao;

import hexafoot.model.EventoPartida;
import hexafoot.model.Jogador;
import hexafoot.model.Partida;
import hexafoot.model.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GerenciadorPenaltis {
    private Random random;

    public GerenciadorPenaltis() {
        this.random = new Random();
    }

    public boolean verificarNecessidadeDeDesempate(Partida partida, boolean isFaseEliminatoria) {
        boolean estaEmpatado = false;
        
        if (partida.getGolsMandante() == partida.getGolsVisitante()) {
            estaEmpatado = true;
        }
        
        if (isFaseEliminatoria == true && estaEmpatado == true) {
            return true;
        } else {
            return false;
        }
    }

    public List<Jogador> definirOrdemDosBatedores(Time time) {
        List<Jogador> batedores = new ArrayList<>();
        
        for (Jogador jogador : time.getTitulares()) {
            if ("Atacante".equals(jogador.getPosicao())) {
                batedores.add(jogador);
            }
        }
        
        for (Jogador jogador : time.getTitulares()) {
            if ("Meio-campista".equals(jogador.getPosicao())) {
                batedores.add(jogador);
            }
        }
        
        for (Jogador jogador : time.getTitulares()) {
            if ("Defensor".equals(jogador.getPosicao())) {
                batedores.add(jogador);
            }
        }
        
        for (Jogador jogador : time.getTitulares()) {
            if ("Goleiro".equals(jogador.getPosicao())) {
                batedores.add(jogador);
            }
        }

        return batedores;
    }

    public void disputarDecisaoPorPenaltis(Partida partida, List<Jogador> batedoresEscolhidos) {
        List<Jogador> batedoresMandante = new ArrayList<>();
        List<Jogador> batedoresVisitante = new ArrayList<>();

        if ("Brasil".equals(partida.getMandante().getNome())) { //verifica se é o Brasil para usar a lista escolhida pelo usuario
            batedoresMandante = batedoresEscolhidos;
        } else {
            batedoresMandante = definirOrdemDosBatedores(partida.getMandante());
        }

        if ("Brasil".equals(partida.getVisitante().getNome())) {
            batedoresVisitante = batedoresEscolhidos;
        } else {
            batedoresVisitante = definirOrdemDosBatedores(partida.getVisitante());
        }

        Jogador goleiroMandante = obterGoleiro(partida.getMandante());
        Jogador goleiroVisitante = obterGoleiro(partida.getVisitante());

        int placarMandante = 0;
        int placarVisitante = 0;
        int cobrancasMandante = 0;
        int cobrancasVisitante = 0;

        for (int i = 0; i < 5; i ++) { //serie de 5 cobrancas
            Jogador batedorMandante = batedoresMandante.get(i);
            boolean fezGolMandante = realizarCobranca(batedorMandante, goleiroVisitante, partida, "Mandante");
            
            if (fezGolMandante == true) {
                placarMandante ++;
            }
            cobrancasMandante ++;

            if (matematicamenteDecidido(placarMandante, placarVisitante, 5 - cobrancasMandante, 5 - cobrancasVisitante) == true) {
                break;
            }

            Jogador batedorVisitante = batedoresVisitante.get(i);
            boolean fezGolVisitante = realizarCobranca(batedorVisitante, goleiroMandante, partida, "Visitante");
            
            if (fezGolVisitante == true) {
                placarVisitante ++;
            }
            cobrancasVisitante ++;

            if (matematicamenteDecidido(placarMandante, placarVisitante, 5 - cobrancasMandante, 5 - cobrancasVisitante) == true) {
                break;
            }
        }

        int rodadaMorteSubita = 5; //morte subita
        while (placarMandante == placarVisitante) {
            Jogador batedorMandante = batedoresMandante.get(rodadaMorteSubita % batedoresMandante.size());
            boolean fezGolMandante = realizarCobranca(batedorMandante, goleiroVisitante, partida, "Mandante");
            if (fezGolMandante == true) {
                placarMandante ++;
            }

            Jogador batedorVisitante = batedoresVisitante.get(rodadaMorteSubita % batedoresVisitante.size());
            boolean fezGolVisitante = realizarCobranca(batedorVisitante, goleiroMandante, partida, "Visitante");
            if (fezGolVisitante == true) {
                placarVisitante ++;
            }

            rodadaMorteSubita ++;
        }
    }

    private boolean realizarCobranca(Jogador batedor, Jogador goleiro, Partida partida, String lado) {
        int ataque = batedor.getAtaque();
        int defesa = goleiro.getDefesa();
        int chanceSucesso = 75 + (ataque / 5) - (defesa / 5);
        
        if (chanceSucesso > 95) {
            chanceSucesso = 95;
        }
        if (chanceSucesso < 30) {
            chanceSucesso = 30; 
        }

        int rolagem = random.nextInt(100) + 1;
        boolean convertido = false;
        
        if (rolagem <= chanceSucesso) {
            convertido = true;
        }

        String tipoEvento = "";
        if (convertido == true) {
            tipoEvento = "PenaltiConvertido" + lado;
        } else {
            tipoEvento = "PenaltiPerdido" + lado;
        }
        
        partida.adicionarEvento(new EventoPartida(120, tipoEvento, batedor));

        return convertido;
    }

    private Jogador obterGoleiro(Time time) {
        for (Jogador jogador : time.getTitulares()) {
            if ("Goleiro".equals(jogador.getPosicao())) {
                return jogador;
            }
        }
        return time.getTitulares().get(0);
    }

    private boolean matematicamenteDecidido(int golsA, int golsB, int restantesA, int restantesB) {
        if (golsA > (golsB + restantesB)) {
            return true;
        }
        if (golsB > (golsA + restantesA)) {
            return true;
        }
        return false;
    }
}