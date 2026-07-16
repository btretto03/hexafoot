package hexafoot.model;
import java.io.Serializable;
import java.util.List;

/**
 * Grupo da primeira fase e sua lista imutável de seleções.
 */
public class Grupo implements Serializable {

    private final String identificador;
    private final List<Time> times;

    /**
     * Cria um grupo copiando a lista recebida e normalizando o identificador.
     *
     * @param identificador código do grupo; espaços nas extremidades são removidos e
     *                      letras são convertidas para maiúsculas
     * @param times seleções do grupo, preservadas na ordem informada
     */
    public Grupo(String identificador, List<Time> times) {
        this.identificador = identificador.trim().toUpperCase();
        this.times = List.copyOf(times);
    }

    public String getIdentificador() {
        return identificador;
    }

    public List<Time> getTimes() {
        return times;
    }

    public boolean contem(Time time) {
        return times.contains(time);
    }
}
