package hexafoot.service.torneio;

import hexafoot.dados.LeitorCSVTorneio;
import hexafoot.model.FaseTorneio;
import hexafoot.model.Grupo;
import hexafoot.model.Partida;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.StatusPartidaTorneio;
import hexafoot.model.Time;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GerenciadorTorneioTest {

    private GerenciadorTorneio gerenciador;
    private Time brasil;

    @BeforeEach
    void criarTorneioComSelecoesDosGrupos() {
        List<Time> internacionais = new ArrayList<>();

        for (String[] grupo : new LeitorCSVTorneio().lerGrupos()) {
            for (int i = 1; i < grupo.length; i++) {
                Time time = new Time(grupo[i]);
                if (grupo[i].equals("Brasil")) {
                    brasil = time;
                } else {
                    internacionais.add(time);
                }
            }
        }

        gerenciador = new GerenciadorTorneio(brasil, internacionais);
    }

    @Test
    void deveIniciarNaPrimeiraRodadaDaFaseDeGrupos() {
        PartidaTorneio proxima = gerenciador.getProximaPartidaBrasil().orElseThrow();

        assertSame(brasil, gerenciador.getBrasil());
        assertEquals(FaseTorneio.FASE_DE_GRUPOS, gerenciador.getFaseAtual());
        assertEquals(1, gerenciador.getRodadaAtual());
        assertEquals(12, gerenciador.getGrupos().size());
        assertEquals(72, gerenciador.getPartidasFaseGrupos().size());
        assertEquals(32, gerenciador.getPartidasMataMata().size());
        assertEquals(24, gerenciador.getPartidasDaRodada(1).size());
        assertEquals("FG-C-R1-J1", proxima.getId());
        assertSame(brasil, proxima.getMandante());
        assertEquals(StatusPartidaTorneio.AGENDADA, proxima.getStatus());
        assertThrows(IllegalArgumentException.class, () -> gerenciador.iniciarPartida("inexistente"));
    }

    @Test
    void deveRegistrarResultadoUmaUnicaVez() {
        PartidaTorneio agendamento = gerenciador.getProximaPartidaBrasil().orElseThrow();
        Time visitante = agendamento.getVisitante();
        Partida partida = gerenciador.iniciarPartida(agendamento.getId());
        partida.adicionarGolMandante();
        partida.adicionarGolMandante();
        partida.adicionarGolVisitante();

        boolean registrado = gerenciador.registrarResultado(agendamento.getId(), partida);
        boolean registradoNovamente = gerenciador.registrarResultado(agendamento.getId(), partida);

        assertTrue(registrado);
        assertFalse(registradoNovamente);
        assertEquals(StatusPartidaTorneio.CONCLUIDA, agendamento.getStatus());
        assertSame(partida, agendamento.getPartida());
        assertEquals(3, brasil.getPontos());
        assertEquals(2, brasil.getGolsMarcados());
        assertEquals(1, brasil.getGolsSofridos());
        assertEquals(1, brasil.getVitorias());
        assertEquals(0, visitante.getPontos());
        assertEquals(1, visitante.getDerrotas());
    }

    @Test
    void deveOrdenarClassificacaoPorPontosSaldoEGols() {
        Grupo grupoA = gerenciador.getGrupos().stream()
                .filter(grupo -> grupo.getIdentificador().equals("A"))
                .findFirst()
                .orElseThrow();
        Time mexico = buscarTime(grupoA, "México");
        Time africaDoSul = buscarTime(grupoA, "África do Sul");
        Time coreiaDoSul = buscarTime(grupoA, "Coreia do Sul");
        Time republicaTcheca = buscarTime(grupoA, "República Tcheca");

        mexico.registrarVitoria();
        mexico.setGolsMarcados(4);
        mexico.setGolsSofridos(1);
        africaDoSul.registrarVitoria();
        africaDoSul.setGolsMarcados(3);
        africaDoSul.setGolsSofridos(2);
        coreiaDoSul.registrarVitoria();
        coreiaDoSul.setGolsMarcados(2);
        coreiaDoSul.setGolsSofridos(1);
        republicaTcheca.registrarEmpate();

        List<Time> classificacao = gerenciador.getClassificacaoGrupo(" a ");

        assertEquals(List.of(mexico, africaDoSul, coreiaDoSul, republicaTcheca), classificacao);
        assertThrows(UnsupportedOperationException.class, () -> classificacao.remove(0));
        assertThrows(IllegalArgumentException.class, () -> gerenciador.getClassificacaoGrupo("Z"));
    }

    private Time buscarTime(Grupo grupo, String nome) {
        return grupo.getTimes().stream()
                .filter(time -> time.getNome().equals(nome))
                .findFirst()
                .orElseThrow();
    }

}
