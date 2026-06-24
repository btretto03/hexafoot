package hexafoot.service.simulacao;

public abstract class ValidadorRegraBase {
    protected String mensagemErro;

    public ValidadorRegraBase() {
        this.mensagemErro = "";
    }
    
    public abstract void validar() throws Exception;

    public String getMensagemErro() {
        return mensagemErro;
    }
}
