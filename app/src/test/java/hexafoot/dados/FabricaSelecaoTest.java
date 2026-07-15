package hexafoot.dados;

import hexafoot.model.Formacao;
import hexafoot.model.Jogador;
import hexafoot.model.Time;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricaSelecaoTest {

    private final FabricaSelecao fabrica = new FabricaSelecao();

    @Test
    void deveInstanciarJogadorComOsCamposDoCsv() {
        Jogador jogador = fabrica.instanciarJogador(
                new String[] {"Alisson", "Liverpool", "Goleiro", "50", "98", "83", "23"});

        assertEquals("Alisson", jogador.getNome());
        assertEquals("Goleiro", jogador.getPosicao());
        assertEquals(50, jogador.getAtaque());
        assertEquals(98, jogador.getDefesa());
        assertEquals(100, jogador.getFisico());
        assertEquals(23, jogador.getEstresse());
    }

    @Test
    void deveRejeitarAtributoNumericoInvalido() {
        String[] campos = {"Alisson", "Liverpool", "Goleiro", "invalido", "98", "83", "23"};

        assertThrows(NumberFormatException.class, () -> fabrica.instanciarJogador(campos));
    }

    @Test
    void deveMontarOnzeTitularesComOMelhorGoleiro() {
        Jogador goleiroFraco = jogador("Goleiro fraco", "Goleiro", 20, 70);
        Jogador goleiroForte = jogador("Goleiro forte", "Goleiro", 20, 95);
        List<Jogador> jogadores = new ArrayList<>(List.of(goleiroFraco, goleiroForte));
        jogadores.addAll(List.of(
                jogador("Defensor 1", "Defensor", 40, 90),
                jogador("Defensor 2", "Defensor", 40, 89),
                jogador("Defensor 3", "Defensor", 40, 88),
                jogador("Defensor 4", "Defensor", 40, 87),
                jogador("Meia 1", "Meio-campista", 80, 70),
                jogador("Meia 2", "Meio-campista", 79, 70),
                jogador("Meia 3", "Meio-campista", 78, 70),
                jogador("Atacante 1", "Atacante", 95, 30),
                jogador("Atacante 2", "Atacante", 94, 30),
                jogador("Atacante 3", "Atacante", 93, 30)));

        Time time = fabrica.montarTime("teste", jogadores);

        assertEquals(Formacao.F_4_3_3, time.getFormacaoAtual());
        assertEquals(11, time.getTitulares().size());
        assertEquals(1, time.getReservas().size());
        assertTrue(time.getTitulares().contains(goleiroForte));
        assertFalse(time.getTitulares().contains(goleiroFraco));
        assertEquals(goleiroFraco, time.getReservas().get(0));
    }

    @Test
    void deveProcessarOsCinquentaJogadoresDoBrasil() {
        List<Jogador> jogadores = fabrica.processarListaBrasil();
        Jogador alisson = jogadores.stream()
                .filter(jogador -> jogador.getNome().equals("Alisson"))
                .findFirst()
                .orElseThrow();

        assertEquals(50, jogadores.size());
        assertEquals("Goleiro", alisson.getPosicao());
        assertEquals(50, alisson.getAtaque());
        assertEquals(98, alisson.getDefesa());
        assertEquals(23, alisson.getEstresse());
    }

    @Test
    void deveMontarTodasAsSelecoesInternacionaisSemOBrasil() {
        List<Time> selecoes = fabrica.processarListasInternacionais();
        Time alemanha = selecoes.stream()
                .filter(time -> time.getNome().equals("alemanha"))
                .findFirst()
                .orElseThrow();

        assertEquals(49, selecoes.size());
        assertTrue(selecoes.stream().noneMatch(time -> time.getNome().equals("brasil")));
        assertEquals(11, alemanha.getTitulares().size());
        assertEquals(15, alemanha.getReservas().size());
    }

    private Jogador jogador(String nome, String posicao, int ataque, int defesa) {
        return new Jogador(nome, posicao, ataque, defesa, 85, 10);
    }
}
