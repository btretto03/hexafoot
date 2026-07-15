package hexafoot.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormacaoTest {

    @Test
    void deveInformarDistribuicaoCorretaDeCadaFormacao() {
        assertDistribuicao(Formacao.F_4_3_3, 4, 3, 3);
        assertDistribuicao(Formacao.F_3_4_3, 3, 4, 3);
        assertDistribuicao(Formacao.F_4_2_4, 4, 2, 4);
        assertDistribuicao(Formacao.F_4_4_2, 4, 4, 2);
        assertDistribuicao(Formacao.F_4_2_3_1, 4, 5, 1);
        assertDistribuicao(Formacao.F_3_5_2, 3, 5, 2);
        assertDistribuicao(Formacao.F_5_4_1, 5, 4, 1);
        assertDistribuicao(Formacao.F_5_3_2, 5, 3, 2);
        assertDistribuicao(Formacao.F_4_5_1, 4, 5, 1);
    }

    @Test
    void deveInformarModificadoresCorretosDeCadaFormacao() {
        assertModificadores(Formacao.F_4_3_3, 1.15, 0.85);
        assertModificadores(Formacao.F_3_4_3, 1.10, 0.90);
        assertModificadores(Formacao.F_4_2_4, 1.20, 0.80);
        assertModificadores(Formacao.F_4_4_2, 1.00, 1.00);
        assertModificadores(Formacao.F_4_2_3_1, 1.05, 0.95);
        assertModificadores(Formacao.F_3_5_2, 0.95, 1.05);
        assertModificadores(Formacao.F_5_4_1, 0.80, 1.20);
        assertModificadores(Formacao.F_5_3_2, 0.85, 1.15);
        assertModificadores(Formacao.F_4_5_1, 0.90, 1.10);
        assertEquals(9, Formacao.values().length);
    }

    private void assertDistribuicao(Formacao formacao, int defensores, int meias, int atacantes) {
        assertEquals(defensores, formacao.getQuantidadeDefensores());
        assertEquals(meias, formacao.getQuantidadeMeio());
        assertEquals(atacantes, formacao.getQuantidadeAtacantes());
    }

    private void assertModificadores(Formacao formacao, double ataque, double defesa) {
        assertEquals(ataque, formacao.getModificadorAtaque(), 0.0001);
        assertEquals(defesa, formacao.getModificadorDefesa(), 0.0001);
    }
}
