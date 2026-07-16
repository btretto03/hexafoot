package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SorteadorJogadorTest {

    private final SorteadorJogador sorteador = new SorteadorJogador();

    @Test
    void deveSortearUnicoPesoDeAtaquePositivo() {
        Jogador semPeso = ApoioTestes.jogador("Sem ataque", "Defensor", 0, 80, 85, 10);
        Jogador unicoComPeso = ApoioTestes.jogador("Com ataque", "Atacante", 1, 20, 85, 10);

        Jogador sorteado = sorteador.sortearPorAtaque(List.of(semPeso, unicoComPeso));

        assertSame(unicoComPeso, sorteado, "O único candidato com peso deveria ser escolhido");
    }

    @Test
    void deveFalharSemPesoDeAtaque() {
        Jogador semPeso = ApoioTestes.jogador("Sem ataque", "Defensor", 0, 80, 85, 10);

        assertThrows(IllegalArgumentException.class,
                () -> sorteador.sortearPorAtaque(List.of(semPeso)),
                "O sorteio deveria rejeitar uma soma de pesos igual a zero");
    }

    @Test
    void deveSortearUnicoPesoDeEstressePositivo() {
        Jogador semPeso = ApoioTestes.jogador("Descansado", "Defensor", 0, 80, 85, 0);
        Jogador unicoComPeso = ApoioTestes.jogador("Estressado", "Defensor", 0, 80, 85, 1);

        Jogador sorteado = sorteador.sortearPorEstresse(List.of(semPeso, unicoComPeso));

        assertSame(unicoComPeso, sorteado, "O único candidato com peso deveria ser escolhido");
    }

    @Test
    void deveFalharSemPesoDeEstresse() {
        Jogador semPeso = ApoioTestes.jogador("Descansado", "Defensor", 0, 80, 85, 0);

        assertThrows(IllegalArgumentException.class,
                () -> sorteador.sortearPorEstresse(List.of(semPeso)),
                "O sorteio deveria rejeitar uma soma de pesos igual a zero");
    }
}
