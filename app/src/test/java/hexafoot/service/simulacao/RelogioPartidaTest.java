package hexafoot.service.simulacao;

import hexafoot.model.Partida;
import hexafoot.model.Time;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RelogioPartidaTest {

    @Test
    void deveNotificarProcessadoresNaOrdem() {
        RelogioPartida relogio = new RelogioPartida();
        List<String> notificacoes = new ArrayList<>();
        ObservadorFalso primeiro = new ObservadorFalso("primeiro", notificacoes);
        ObservadorFalso segundo = new ObservadorFalso("segundo", notificacoes);
        relogio.adicionarProcessador(primeiro);
        relogio.adicionarProcessador(segundo);
        Partida partida = new Partida(new Time("Mandante"), new Time("Visitante"));

        relogio.processarMinutoIsolado(37, partida);

        assertEquals(List.of("primeiro:37", "segundo:37"), notificacoes,
                "A ordem de registro deveria ser preservada");
        assertEquals(1, primeiro.getQuantidadeChamadas(), "O primeiro observer deveria ser chamado uma vez");
        assertEquals(1, segundo.getQuantidadeChamadas(), "O segundo observer deveria ser chamado uma vez");
        assertSame(partida, primeiro.getUltimaPartida(), "A mesma partida deveria ser entregue ao observer");
    }

    @Test
    void deveNotificarNovamenteNoMesmoMinuto() {
        RelogioPartida relogio = new RelogioPartida();
        List<String> notificacoes = new ArrayList<>();
        ObservadorFalso observer = new ObservadorFalso("observer", notificacoes);
        relogio.adicionarProcessador(observer);
        Partida partida = new Partida(new Time("Mandante"), new Time("Visitante"));

        relogio.processarMinutoIsolado(45, partida);
        relogio.processarMinutoIsolado(45, partida);

        assertEquals(List.of("observer:45", "observer:45"), notificacoes,
                "As duas chamadas deveriam ser encaminhadas");
        assertEquals(2, observer.getQuantidadeChamadas(), "O observer deveria ser chamado duas vezes");
    }

    private static final class ObservadorFalso implements ObserverMinuto {
        private final String nome;
        private final List<String> notificacoes;
        private int quantidadeChamadas;
        private Partida ultimaPartida;

        private ObservadorFalso(String nome, List<String> notificacoes) {
            this.nome = nome;
            this.notificacoes = notificacoes;
        }

        @Override
        public void atualizarMinuto(int minutoAtual, Partida partida) {
            notificacoes.add(nome + ":" + minutoAtual);
            quantidadeChamadas++;
            ultimaPartida = partida;
        }

        private int getQuantidadeChamadas() {
            return quantidadeChamadas;
        }

        private Partida getUltimaPartida() {
            return ultimaPartida;
        }
    }
}
