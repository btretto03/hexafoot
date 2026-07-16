package hexafoot.service.simulacao;
import hexafoot.model.exception.ElencoIncompletoException;
import hexafoot.model.Time;

/**
 * Valida a quantidade total de atletas convocados.
 */
public class ValidadorConvocacao extends ValidadorRegraBase{
     private Time elencoOficial;

    public ValidadorConvocacao(Time elencoOficial) {
        this.elencoOficial = elencoOficial;
    }

    /**
     * Exige exatamente 26 jogadores, somando titulares e reservas.
     *
     * @throws ElencoIncompletoException se a quantidade for diferente de 26
     */
    @Override
    public void validar() throws ElencoIncompletoException {
        int total = elencoOficial.getTitulares().size() + elencoOficial.getReservas().size();

        if (total != 26) {
            this.mensagemErro = "Elenco incompleto: " + total + " de 26";
            throw new ElencoIncompletoException(total);
        }
    }
}
