package hexafoot.service.simulacao;

/**
 * Base para validações que preservam uma mensagem descritiva da última falha.
 */
public abstract class ValidadorRegraBase {
    protected String mensagemErro;

    public ValidadorRegraBase() {
        this.mensagemErro = "";
    }
    
    /**
     * Verifica a regra e, em caso de falha, preenche {@code mensagemErro} antes
     * de lançar a exceção específica.
     *
     * @throws Exception quando a regra não for atendida
     */
    public abstract void validar() throws Exception;

    public String getMensagemErro() {
        return mensagemErro;
    }
}
