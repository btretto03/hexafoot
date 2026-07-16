package hexafoot.model;

import hexafoot.model.strategy.TaticaEquilibrada;
import hexafoot.model.strategy.TaticaOfensiva;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeTest {

    @Test
    void deveIniciarComElencoVazioConfiguracaoPadraoEEstatisticasZeradas() {
        Time time = new Time("Brasil");

        assertEquals("Brasil", time.getNome());
        assertTrue(time.getTitulares().isEmpty());
        assertTrue(time.getReservas().isEmpty());
        assertInstanceOf(TaticaEquilibrada.class, time.getTaticaAtual());
        assertEquals(Formacao.F_4_3_3, time.getFormacaoAtual());
        assertEquals(0, time.getPontos());
        assertEquals(0, time.getGolsMarcados());
        assertEquals(0, time.getGolsSofridos());
        assertEquals(0, time.getVitorias());
        assertEquals(0, time.getEmpates());
        assertEquals(0, time.getDerrotas());
    }

    @Test
    void deveImpedirDuplicidadeEntreTitularesEReservasELimitarTitulares() {
        Time time = new Time("Brasil");
        Jogador primeiro = jogador("Titular 0", "Atacante", 80, 40);
        time.adicionarTitular(primeiro);

        time.adicionarTitular(jogador("Titular 0", "Defensor", 10, 99));
        time.adicionarReserva(primeiro);

        for (int i = 1; i < 12; i++) {
            time.adicionarTitular(jogador("Titular " + i, "Atacante", 80, 40));
        }

        assertEquals(11, time.getTitulares().size());
        assertTrue(time.getTitulares().contains(primeiro));
        assertTrue(time.getReservas().isEmpty());
        assertFalse(time.getTitulares().stream()
                        .anyMatch(jogador -> jogador.getNome().equals("Titular 11")));
    }

    @Test
    void deveAdicionarERemoverReservaComRetornoExplicito() {
        Time time = new Time("Brasil");
        Jogador reserva = jogador("Reserva", "Defensor", 40, 80);

        time.adicionarReserva(reserva);
        time.adicionarReserva(jogador("Reserva", "Atacante", 90, 20));

        assertEquals(1, time.getReservas().size());
        assertTrue(time.removerReserva(reserva));
        assertFalse(time.removerReserva(reserva));
        assertFalse(time.removerTitular(reserva));
    }

    @Test
    void deveCalcularForcasComFisicoStatusTaticaEFormacao() {
        Time time = new Time("Brasil");
        Jogador inteiro = jogador("Inteiro", "Atacante", 100, 80);
        Jogador cansado = jogador("Cansado", "Defensor", 80, 100);
        Jogador suspenso = jogador("Suspenso", "Atacante", 1000, 1000);
        cansado.setFisico(0);
        suspenso.setStatus("Suspenso");
        time.adicionarTitular(inteiro);
        time.adicionarTitular(cansado);
        time.adicionarTitular(suspenso);

        assertEquals(161, time.calcularForcaAtaqueAtual());
        assertEquals(111, time.calcularForcaDefesaAtual());

        time.setTaticaAtual(new TaticaOfensiva());
        time.setFormacaoAtual(Formacao.F_4_4_2);

        assertEquals(154, time.calcularForcaAtaqueAtual());
        assertEquals(117, time.calcularForcaDefesaAtual());
    }

    @Test
    void deveAtualizarEstatisticasEPontuacao() {
        Time time = new Time("Brasil");
        time.setGolsMarcados(7);
        time.setGolsSofridos(4);

        time.registrarVitoria();
        time.registrarEmpate();
        time.registrarDerrota();

        assertEquals(4, time.getPontos());
        assertEquals(1, time.getVitorias());
        assertEquals(1, time.getEmpates());
        assertEquals(1, time.getDerrotas());
        assertEquals(3, time.getSaldoGols());
    }

    @Test
    void deveEscalarOsMelhoresPorPosicaoSegundoAFormacao() {
        Time time = new Time("Brasil");
        time.setFormacaoAtual(Formacao.F_4_4_2);

        Jogador goleiroForte = jogador("Goleiro forte", "Goleiro", 10, 95);
        Jogador goleiroFraco = jogador("Goleiro fraco", "Goleiro", 10, 70);
        Jogador defensor1 = jogador("Defensor 1", "Defensor", 30, 95);
        Jogador defensor2 = jogador("Defensor 2", "Defensor", 30, 90);
        Jogador defensor3 = jogador("Defensor 3", "Defensor", 30, 85);
        Jogador defensor4 = jogador("Defensor 4", "Defensor", 30, 80);
        Jogador defensorFraco = jogador("Defensor fraco", "Defensor", 30, 75);
        Jogador meia1 = jogador("Meia 1", "Meio-campista", 80, 50);
        Jogador meia2 = jogador("Meia 2", "Meio-campista", 70, 50);
        Jogador meia3 = jogador("Meia 3", "Meio-campista", 60, 50);
        Jogador meia4 = jogador("Meia 4", "Meio-campista", 50, 50);
        Jogador meiaFraco = jogador("Meia fraco", "Meio-campista", 40, 40);
        Jogador atacante1 = jogador("Atacante 1", "Atacante", 95, 20);
        Jogador atacante2 = jogador("Atacante 2", "Atacante", 90, 20);
        Jogador atacanteFraco = jogador("Atacante fraco", "Atacante", 85, 20);
        atacante1.setStatus("Suspenso");

        List<Jogador> elenco = List.of(goleiroFraco, goleiroForte,
                        defensorFraco, defensor4, defensor3, defensor2, defensor1,
                        meiaFraco, meia4, meia3, meia2, meia1,
                        atacanteFraco, atacante2, atacante1);
        elenco.forEach(time::adicionarReserva);

        time.escalarMelhoresJogadores();

        assertEquals(11, time.getTitulares().size());
        assertEquals(4, time.getReservas().size());
        assertTrue(time.getTitulares().containsAll(List.of(goleiroForte,
                        defensor1, defensor2, defensor3, defensor4,
                        meia1, meia2, meia3, meia4, atacante1, atacante2)));
        assertTrue(time.getReservas().containsAll(List.of(goleiroFraco,
                        defensorFraco, meiaFraco, atacanteFraco)));
    }

    @Test
    void deveCompletarTitularesComSobrasQuandoFaltaremPosicoes() {
        Time time = new Time("Brasil");
        List<Jogador> coringas = new ArrayList<>();

        for (int i = 0; i < 11; i++) {
            Jogador jogador = jogador("Coringa " + i, "Ala", 50 + i, 50);
            coringas.add(jogador);
            time.adicionarReserva(jogador);
        }

        time.escalarMelhoresJogadores();

        assertEquals(11, time.getTitulares().size());
        assertTrue(time.getTitulares().containsAll(coringas));
        assertTrue(time.getReservas().isEmpty());
    }

    private Jogador jogador(String nome, String posicao, int ataque, int defesa) {
        return new Jogador(nome, posicao, ataque, defesa, 85, 20);
    }
}
