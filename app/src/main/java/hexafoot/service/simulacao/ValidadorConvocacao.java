package hexafoot.service.simulacao;
import hexafoot.model.exception.ElencoIncompletoException;
import hexafoot.model.Time;

public class ValidadorConvocacao extends ValidadorRegraBase{
     private Time elencoOficial;

    public ValidadorConvocacao(Time elencoOficial) {
        this.elencoOficial = elencoOficial;
    }

    @Override
    public void validar() throws ElencoIncompletoException {
        int total = elencoOficial.getTitulares().size() + elencoOficial.getReservas().size();

        if (total != 26) {
            this.mensagemErro = "Elenco incompleto: " + total + " de 26";
            throw new ElencoIncompletoException(total);
        }
    }
}
