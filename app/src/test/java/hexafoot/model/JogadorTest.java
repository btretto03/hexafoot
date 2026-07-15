package hexafoot.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JogadorTest {

    @Test
    void deveIniciarDescansadoAtivoESemPunicoes() {
        Jogador jogador = new Jogador("Ronaldo", "Atacante", 92, 40, 73, 25);

        assertEquals("Ronaldo", jogador.getNome());
        assertEquals("Atacante", jogador.getPosicao());
        assertEquals(92, jogador.getAtaque());
        assertEquals(40, jogador.getDefesa());
        assertEquals(100, jogador.getFisico());
        assertEquals(25, jogador.getEstresse());
        assertEquals(0, jogador.getCartoesAmarelos());
        assertEquals(0, jogador.getRodadasAfastamento());
        assertEquals("Ativo", jogador.getStatus());
    }

    @Test
    void deveSuspenderAoReceberSegundoCartaoAmarelo() {
        Jogador jogador = novoJogador();

        jogador.aplicarCartaoAmarelo();

        assertEquals(1, jogador.getCartoesAmarelos());
        assertEquals(0, jogador.getRodadasAfastamento());
        assertEquals("Ativo", jogador.getStatus());

        jogador.aplicarCartaoAmarelo();

        assertEquals(2, jogador.getCartoesAmarelos());
        assertEquals(1, jogador.getRodadasAfastamento());
        assertEquals("Suspenso", jogador.getStatus());
    }

    @Test
    void deveAplicarVermelhoSemApagarAmarelos() {
        Jogador jogador = novoJogador();
        jogador.aplicarCartaoAmarelo();

        jogador.aplicarCartaoVermelho();

        assertEquals(1, jogador.getCartoesAmarelos());
        assertEquals(1, jogador.getRodadasAfastamento());
        assertEquals("Suspenso", jogador.getStatus());
    }

    @Test
    void deveReativarSomenteQuandoAfastamentoChegarAZero() {
        Jogador jogador = novoJogador();
        jogador.sofrerLesao(2);

        assertEquals("Lesionado", jogador.getStatus());
        assertEquals(2, jogador.getRodadasAfastamento());

        jogador.atualizarLesao();

        assertEquals("Lesionado", jogador.getStatus());
        assertEquals(1, jogador.getRodadasAfastamento());

        jogador.atualizarLesao();

        assertEquals("Ativo", jogador.getStatus());
        assertEquals(0, jogador.getRodadasAfastamento());
    }

    @Test
    void deveAcumularFracaoDeDesgasteEntreChamadas() {
        Jogador jogador = new Jogador("Atacante", "Atacante", 80, 30, 85, 10);

        jogador.consumirEnergia(90, 0.25f);
        assertEquals(98, jogador.getFisico());

        jogador.consumirEnergia(90, 0.25f);
        assertEquals(95, jogador.getFisico());
    }

    @Test
    void deveAplicarTaxaDeDesgastePorPosicaoESemUltrapassarZero() {
        Jogador goleiro = new Jogador("Goleiro", "Goleiro", 20, 90, 85, 10);
        Jogador defensor = new Jogador("Defensor", "Defensor", 40, 80, 85, 10);
        Jogador posicaoDesconhecida = new Jogador("Outro", "Ala", 70, 70, 85, 10);

        goleiro.consumirEnergia(90, 1.0f);
        defensor.consumirEnergia(90, 1.0f);
        posicaoDesconhecida.consumirEnergia(90, 1.0f);

        assertEquals(98, goleiro.getFisico());
        assertEquals(94, defensor.getFisico());
        assertEquals(100, posicaoDesconhecida.getFisico());

        defensor.consumirEnergia(9000, 1.0f);
        assertEquals(0, defensor.getFisico());
    }

    @Test
    void deveLimitarFisicoERecuperarReservasMaisQueTitulares() {
        Jogador jogador = novoJogador();

        jogador.setFisico(-5);
        assertEquals(0, jogador.getFisico());

        jogador.recuperarEnergiaPosJogo(true);
        assertEquals(2, jogador.getFisico());

        jogador.recuperarEnergiaPosJogo(false);
        assertEquals(12, jogador.getFisico());

        jogador.setFisico(95);
        jogador.recuperarEnergiaPosJogo(false);
        assertEquals(100, jogador.getFisico());

        jogador.setFisico(150);
        assertEquals(100, jogador.getFisico());
    }

    @Test
    void deveCompararJogadoresExclusivamentePeloNome() {
        Jogador original = new Jogador("Alex", "Defensor", 30, 90, 85, 20);
        Jogador mesmoNome = new Jogador("Alex", "Atacante", 99, 10, 50, 80);
        Jogador caixaDiferente = new Jogador("alex", "Defensor", 30, 90, 85, 20);

        assertTrue(original.equals(original));
        assertTrue(original.equals(mesmoNome));
        assertFalse(original.equals(caixaDiferente));
        assertFalse(original.equals(null));
        assertFalse(original.equals("Alex"));
    }

    private Jogador novoJogador() {
        return new Jogador("Jogador", "Meio-campista", 70, 70, 85, 20);
    }
}
