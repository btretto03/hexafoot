package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Partida;
import hexafoot.model.Time;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessadorFisicoTest {

    @Test
    void deveConsumirEnergiaDosJogadoresAtivos() {
        Time mandante = new Time("Mandante");
        Jogador atacanteAtivo = ApoioTestes.jogador("Ativo", "Atacante", 80, 40, 85, 10);
        Jogador atacanteSuspenso = ApoioTestes.jogador("Suspenso", "Atacante", 80, 40, 85, 10);
        atacanteSuspenso.aplicarCartaoVermelho();
        mandante.adicionarTitular(atacanteAtivo);
        mandante.adicionarTitular(atacanteSuspenso);

        Time visitante = new Time("Visitante");
        Jogador defensorAtivo = ApoioTestes.jogador("Defensor", "Defensor", 40, 80, 85, 10);
        visitante.adicionarTitular(defensorAtivo);
        Partida partida = new Partida(mandante, visitante);
        ProcessadorFisico processador = new ProcessadorFisico();

        for (int minuto = 1; minuto <= 90; minuto++) {
            processador.atualizarMinuto(minuto, partida);
        }

        assertEquals(90, atacanteAtivo.getFisico(), "O atacante ativo deveria sofrer desgaste");
        assertEquals(100, atacanteSuspenso.getFisico(), "O suspenso não deveria perder energia");
        assertEquals(94, defensorAtivo.getFisico(), "O defensor ativo deveria sofrer desgaste");
    }
}
