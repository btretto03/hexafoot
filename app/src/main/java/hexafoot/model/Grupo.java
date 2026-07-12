package hexafoot.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Grupo da primeira fase, composto por exatamente quatro seleções.
 */
public class Grupo {
    private static final int QUANTIDADE_TIMES = 4;

    private final String identificador;
    private final List<Time> times;

    public Grupo(String identificador, List<Time> times) {
        this.identificador = validarIdentificador(identificador);
        Objects.requireNonNull(times, "A lista de times do grupo não pode ser nula");

        if (times.size() != QUANTIDADE_TIMES) {
            throw new IllegalArgumentException("Um grupo deve possuir exatamente quatro times");
        }

        for (Time time : times) {
            Objects.requireNonNull(time, "Os times do grupo não podem ser nulos");
        }

        Set<Time> timesDistintos = new HashSet<>(times);
        if (timesDistintos.size() != QUANTIDADE_TIMES) {
            throw new IllegalArgumentException("Um grupo não pode possuir times repetidos");
        }

        this.times = List.copyOf(times);
    }

    private String validarIdentificador(String identificador) {
        Objects.requireNonNull(identificador, "O identificador do grupo não pode ser nulo");
        String normalizado = identificador.trim().toUpperCase();

        if (normalizado.length() != 1 || normalizado.charAt(0) < 'A' || normalizado.charAt(0) > 'L') {
            throw new IllegalArgumentException("O identificador do grupo deve estar entre A e L");
        }

        return normalizado;
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
