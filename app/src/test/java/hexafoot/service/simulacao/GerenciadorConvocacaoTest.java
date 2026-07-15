package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GerenciadorConvocacaoTest {

    @Test
    void deveDistribuirConvocadosEntreTitularesEReservas() {
        List<Jogador> disponiveis = criarJogadores(12);
        GerenciadorConvocacao gerenciador = new GerenciadorConvocacao(disponiveis);
        List<Jogador> convocados = new ArrayList<>(disponiveis);

        convocados.forEach(gerenciador::inserirNoElenco);

        assertEquals(11, gerenciador.getElencoOficial().getTitulares().size(),
                "Os onze primeiros deveriam ser titulares");
        assertEquals(1, gerenciador.getElencoOficial().getReservas().size(),
                "O décimo segundo deveria ser reserva");
        assertEquals(convocados.subList(0, 11), gerenciador.getElencoOficial().getTitulares(),
                "A ordem dos titulares deveria seguir a convocação");
        assertEquals(List.of(convocados.get(11)), gerenciador.getElencoOficial().getReservas(),
                "O décimo segundo convocado deveria ficar na reserva");
        assertTrue(disponiveis.isEmpty(), "Todos os convocados deveriam sair dos disponíveis");
    }

    @Test
    void deveManterOVigesimoSetimoJogadorDisponivel() {
        List<Jogador> disponiveis = criarJogadores(27);
        GerenciadorConvocacao gerenciador = new GerenciadorConvocacao(disponiveis);
        List<Jogador> candidatos = new ArrayList<>(disponiveis);

        candidatos.forEach(gerenciador::inserirNoElenco);

        int totalConvocados = gerenciador.getElencoOficial().getTitulares().size()
                + gerenciador.getElencoOficial().getReservas().size();
        assertEquals(26, totalConvocados, "O elenco deveria parar em vinte e seis jogadores");
        assertEquals(1, disponiveis.size(), "Um jogador deveria continuar disponível");
        assertTrue(disponiveis.contains(candidatos.get(26)), "O último candidato não deveria ser convocado");
        assertTrue(gerenciador.validarTamanhoConvocacao(), "O elenco com vinte e seis deveria ser válido");
    }

    @Test
    void deveDevolverReservaRemovidoAosDisponiveis() {
        List<Jogador> disponiveis = criarJogadores(12);
        GerenciadorConvocacao gerenciador = new GerenciadorConvocacao(disponiveis);
        List<Jogador> candidatos = new ArrayList<>(disponiveis);
        candidatos.forEach(gerenciador::inserirNoElenco);
        Jogador reserva = candidatos.get(11);

        gerenciador.removerDoElenco(reserva);

        assertFalse(gerenciador.getElencoOficial().getReservas().contains(reserva),
                "O jogador deveria sair da reserva");
        assertTrue(disponiveis.contains(reserva), "O jogador deveria voltar aos disponíveis");
        assertFalse(gerenciador.validarTamanhoConvocacao(), "Um elenco com onze jogadores deveria ser inválido");
    }

    private List<Jogador> criarJogadores(int quantidade) {
        List<Jogador> jogadores = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            jogadores.add(ApoioTestes.jogador("Jogador " + i, "Atacante"));
        }
        return jogadores;
    }
}
