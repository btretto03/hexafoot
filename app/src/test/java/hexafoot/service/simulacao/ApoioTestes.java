package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Time;

final class ApoioTestes {
    private ApoioTestes() {
    }

    static Jogador jogador(String nome, String posicao) {
        return jogador(nome, posicao, 70, 70, 85, 10);
    }

    static Jogador jogador(String nome, String posicao, int ataque, int defesa,
                           int resistenciaFisica, int estresse) {
        return new Jogador(nome, posicao, ataque, defesa, resistenciaFisica, estresse);
    }

    static Time timeComTitulares(String nome, int quantidade) {
        Time time = new Time(nome);
        for (int i = 1; i <= quantidade; i++) {
            time.adicionarTitular(jogador(nome + " Titular " + i, "Atacante"));
        }
        return time;
    }

    static Time elencoComQuantidade(String nome, int quantidade) {
        Time time = new Time(nome);
        for (int i = 1; i <= quantidade; i++) {
            Jogador jogador = jogador(nome + " Jogador " + i, "Atacante");
            if (i <= 11) {
                time.adicionarTitular(jogador);
            } else {
                time.adicionarReserva(jogador);
            }
        }
        return time;
    }
}
