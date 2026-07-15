package hexafoot.service.simulacao;
import hexafoot.model.Jogador;
import hexafoot.model.exception.JogadorIndisponivelException;
import hexafoot.model.Time;

public class ValidadorEscalacao extends ValidadorRegraBase {
    private Time time;

    public ValidadorEscalacao(Time time) {
        this.time = time;
    }

    @Override
    public void validar() throws JogadorIndisponivelException {
        if (time.getTitulares().size() != 11) {
            this.mensagemErro = "Escalação precisa ter 11 titulares";
            throw new JogadorIndisponivelException("Escalação", "precisa ter exatamente 11 titulares");
        }

        for (Jogador jogador : time.getTitulares()) {
            if ("Ativo".equals(jogador.getStatus()) == false) {
                this.mensagemErro = jogador.getNome() + " está " + jogador.getStatus();
                throw new JogadorIndisponivelException(jogador.getNome(), jogador.getStatus());
            }
        }
    }
}
