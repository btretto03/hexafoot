package hexafoot.model;
import java.util.List;

/**
 * Grupo da primeira fase, composto por exatamente quatro seleções.
 */
public class Grupo {

    private final String identificador;
    private final List<Time> times;

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
