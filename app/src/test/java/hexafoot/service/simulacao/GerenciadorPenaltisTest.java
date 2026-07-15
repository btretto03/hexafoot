package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Partida;
import hexafoot.model.Time;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GerenciadorPenaltisTest {

    private final GerenciadorPenaltis gerenciador = new GerenciadorPenaltis();

    @Test
    void deveReconhecerNecessidadeDeDesempate() {
        Partida empate = new Partida(new Time("A"), new Time("B"));
        Partida vitoriaMandante = new Partida(new Time("C"), new Time("D"));
        vitoriaMandante.adicionarGolMandante();

        assertTrue(gerenciador.verificarNecessidadeDeDesempate(empate, true),
                "Um empate eliminatório deveria exigir pênaltis");
        assertFalse(gerenciador.verificarNecessidadeDeDesempate(empate, false),
                "Um empate na fase de grupos não deveria exigir pênaltis");
        assertFalse(gerenciador.verificarNecessidadeDeDesempate(vitoriaMandante, true),
                "Uma partida com vencedor não deveria exigir pênaltis");
    }

    @Test
    void deveOrdenarBatedoresPorPosicao() {
        Time time = new Time("Brasil");
        Jogador goleiro = ApoioTestes.jogador("Goleiro", "Goleiro");
        Jogador meiaUm = ApoioTestes.jogador("Meia 1", "Meio-Campo");
        Jogador defensor = ApoioTestes.jogador("Defensor", "Defensor");
        Jogador atacante = ApoioTestes.jogador("Atacante", "Atacante");
        Jogador meiaDois = ApoioTestes.jogador("Meia 2", "Meio-campista");
        Jogador ala = ApoioTestes.jogador("Ala", "Ala");
        List.of(goleiro, meiaUm, defensor, atacante, meiaDois, ala).forEach(time::adicionarTitular);

        List<Jogador> ordem = gerenciador.definirOrdemDosBatedores(time);

        assertEquals(List.of(atacante, meiaUm, meiaDois, defensor, goleiro, ala), ordem,
                "A ordem deveria seguir as categorias previstas");
    }

    @Test
    void deveObterGoleiroComAlternativaDeterministica() {
        Time comGoleiro = new Time("Com goleiro");
        Jogador atacante = ApoioTestes.jogador("Atacante", "Atacante");
        Jogador goleiro = ApoioTestes.jogador("Goleiro", "Goleiro");
        comGoleiro.adicionarTitular(atacante);
        comGoleiro.adicionarTitular(goleiro);
        Time semGoleiro = new Time("Sem goleiro");
        Jogador primeiro = ApoioTestes.jogador("Primeiro", "Defensor");
        semGoleiro.adicionarTitular(primeiro);

        assertSame(goleiro, gerenciador.obterGoleiro(comGoleiro),
                "O goleiro de posição deveria ser escolhido");
        assertSame(primeiro, gerenciador.obterGoleiro(semGoleiro),
                "O primeiro titular deveria ser a alternativa");
        assertThrows(IndexOutOfBoundsException.class, () -> gerenciador.obterGoleiro(new Time("Vazio")),
                "Um time sem titulares deveria falhar");
    }

    @Test
    void deveDetectarDecisaoMatematica() {
        assertTrue(gerenciador.matematicamenteDecidido(4, 0, 1, 1),
                "B não conseguiria alcançar A");
        assertTrue(gerenciador.matematicamenteDecidido(0, 4, 1, 1),
                "A não conseguiria alcançar B");
        assertFalse(gerenciador.matematicamenteDecidido(3, 1, 0, 2),
                "A possibilidade de empate deveria manter a disputa aberta");
        assertFalse(gerenciador.matematicamenteDecidido(2, 2, 0, 0),
                "Um empate não deveria ser tratado como decisão matemática");
    }
}
