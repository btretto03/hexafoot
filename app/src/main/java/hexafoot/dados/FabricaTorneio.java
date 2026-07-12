package hexafoot.dados;

import hexafoot.model.Grupo;
import hexafoot.model.Time;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Transforma os dados dos CSVs do torneio em objetos do domínio.
 */
public class FabricaTorneio {
    private static final int QUANTIDADE_GRUPOS = 12;
    private static final int QUANTIDADE_SELECOES = 48;

    private final LeitorCSVTorneio leitor;

    public FabricaTorneio() {
        this.leitor = new LeitorCSVTorneio();
    }

    public List<Grupo> montarGrupos(List<Time> selecoes) {
        Objects.requireNonNull(selecoes, "A lista de seleções não pode ser nula");
        if (selecoes.size() != QUANTIDADE_SELECOES) {
            throw new IllegalArgumentException("A Copa deve possuir exatamente 48 seleções");
        }

        Map<String, Time> selecoesPorNome = indexarSelecoes(selecoes);
        List<Grupo> grupos = new ArrayList<>();
        Set<Time> selecoesAlocadas = new HashSet<>();

        for (String[] campos : leitor.lerGrupos()) {
            List<Time> timesDoGrupo = new ArrayList<>();

            for (int i = 1; i < campos.length; i++) {
                Time time = selecoesPorNome.get(normalizarNome(campos[i]));
                if (time == null) {
                    throw new IllegalStateException(
                            "Seleção do arquivo de grupos não encontrada: " + campos[i]);
                }
                if (!selecoesAlocadas.add(time)) {
                    throw new IllegalStateException(
                            "Seleção repetida no arquivo de grupos: " + campos[i]);
                }

                timesDoGrupo.add(time);
            }

            grupos.add(new Grupo(campos[0], timesDoGrupo));
        }

        if (grupos.size() != QUANTIDADE_GRUPOS || selecoesAlocadas.size() != QUANTIDADE_SELECOES) {
            throw new IllegalStateException("O arquivo deve definir 12 grupos e 48 seleções");
        }

        return List.copyOf(grupos);
    }

    private Map<String, Time> indexarSelecoes(List<Time> selecoes) {
        Map<String, Time> selecoesPorNome = new HashMap<>();

        for (Time time : selecoes) {
            Objects.requireNonNull(time, "As seleções não podem ser nulas");
            String nomeNormalizado = normalizarNome(time.getNome());
            Time anterior = selecoesPorNome.putIfAbsent(nomeNormalizado, time);

            if (anterior != null) {
                throw new IllegalArgumentException("Existem seleções com o mesmo nome: " + time.getNome());
            }
        }

        return selecoesPorNome;
    }

    private String normalizarNome(String nome) {
        Objects.requireNonNull(nome, "O nome da seleção não pode ser nulo");
        String semAcentos = Normalizer.normalize(nome.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        return semAcentos.toLowerCase(Locale.ROOT).replace(' ', '_');
    }
}
