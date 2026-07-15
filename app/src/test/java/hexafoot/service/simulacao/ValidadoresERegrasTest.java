package hexafoot.service.simulacao;

import hexafoot.model.Jogador;
import hexafoot.model.Time;
import hexafoot.model.exception.ElencoIncompletoException;
import hexafoot.model.exception.JogadorIndisponivelException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadoresERegrasTest {

    @Test
    void deveAceitarConvocacaoCompleta() {
        ValidadorConvocacao validador = new ValidadorConvocacao(
                ApoioTestes.elencoComQuantidade("Brasil", 26));

        assertDoesNotThrow(validador::validar, "Uma convocação completa deveria ser válida");
        assertEquals("", validador.getMensagemErro(), "Não deveria haver mensagem de erro");
    }

    @Test
    void deveRejeitarConvocacaoIncompleta() {
        ValidadorConvocacao validador = new ValidadorConvocacao(
                ApoioTestes.elencoComQuantidade("Brasil", 25));

        ElencoIncompletoException excecao = assertThrows(ElencoIncompletoException.class,
                validador::validar, "Uma convocação incompleta deveria falhar");

        assertEquals("Elenco incompleto: 25 de 26", validador.getMensagemErro(),
                "A mensagem interna deveria informar a contagem");
        assertEquals("Elenco incompleto: 25 convocados de 26 necessários.", excecao.getMessage(),
                "A exceção deveria informar a quantidade necessária");
    }

    @Test
    void deveRejeitarConvocacaoAcimaDoLimite() {
        ValidadorConvocacao validador = new ValidadorConvocacao(
                ApoioTestes.elencoComQuantidade("Brasil", 27));

        ElencoIncompletoException excecao = assertThrows(ElencoIncompletoException.class,
                validador::validar, "Uma convocação acima do limite deveria falhar");

        assertEquals("Elenco incompleto: 27 de 26", validador.getMensagemErro());
        assertEquals("Elenco incompleto: 27 convocados de 26 necessários.", excecao.getMessage());
    }

    @Test
    void deveAceitarEscalacaoCompletaEAtiva() {
        ValidadorEscalacao validador = new ValidadorEscalacao(
                ApoioTestes.timeComTitulares("Brasil", 11));

        assertDoesNotThrow(validador::validar, "Onze titulares ativos deveriam ser válidos");
        assertEquals("", validador.getMensagemErro(), "Não deveria haver mensagem de erro");
    }

    @Test
    void deveRejeitarQuantidadeIncorretaDeTitulares() {
        ValidadorEscalacao validador = new ValidadorEscalacao(
                ApoioTestes.timeComTitulares("Brasil", 10));

        JogadorIndisponivelException excecao = assertThrows(JogadorIndisponivelException.class,
                validador::validar, "Uma escalação com dez titulares deveria falhar");

        assertEquals("Escalação precisa ter 11 titulares", validador.getMensagemErro(),
                "A mensagem interna deveria explicar o limite");
        assertEquals("Escalação não pode jogar: precisa ter exatamente 11 titulares", excecao.getMessage(),
                "A exceção deveria explicar a regra violada");
    }

    @Test
    void deveRejeitarMaisDeOnzeTitulares() {
        Time time = ApoioTestes.timeComTitulares("Brasil", 11);
        time.getTitulares().add(ApoioTestes.jogador("Excedente", "Atacante"));
        ValidadorEscalacao validador = new ValidadorEscalacao(time);

        assertThrows(JogadorIndisponivelException.class, validador::validar,
                "Uma escalação acima do limite deveria falhar");
        assertEquals("Escalação precisa ter 11 titulares", validador.getMensagemErro());
    }

    @Test
    void deveRejeitarJogadorSuspenso() {
        Time time = ApoioTestes.timeComTitulares("Brasil", 11);
        Jogador suspenso = time.getTitulares().get(0);
        suspenso.aplicarCartaoVermelho();
        ValidadorEscalacao validador = new ValidadorEscalacao(time);

        JogadorIndisponivelException excecao = assertThrows(JogadorIndisponivelException.class,
                validador::validar, "Um titular suspenso deveria falhar");

        assertEquals(suspenso.getNome() + " está Suspenso", validador.getMensagemErro(),
                "A mensagem interna deveria identificar o jogador");
        assertEquals(suspenso.getNome() + " não pode jogar: Suspenso", excecao.getMessage(),
                "A exceção deveria identificar o motivo");
    }

}
