package hexafoot.dados;

import hexafoot.model.Grupo;
import hexafoot.model.PartidaTorneio;
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
    private static final int QUANTIDADE_PARTIDAS_FASE_GRUPOS = 72;
    private static final int PARTIDAS_POR_GRUPO = 6;
    private static final int PARTIDAS_POR_RODADA = 2;

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
                    throw new IllegalStateException("Seleção do arquivo de grupos não encontrada: " + campos[i]);
                }
                if (!selecoesAlocadas.add(time)) {
                    throw new IllegalStateException("Seleção repetida no arquivo de grupos: " + campos[i]);
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

    public List<PartidaTorneio> montarCalendarioFaseGrupos(List<Grupo> grupos) {
        Map<String, Grupo> gruposPorIdentificador = indexarGrupos(grupos);
        Map<String, Integer> partidasPorGrupo = new HashMap<>();
        Map<String, Integer> partidasPorRodada = new HashMap<>();
        Set<String> confrontos = new HashSet<>();
        List<PartidaTorneio> partidas = new ArrayList<>();

        for (String[] campos : leitor.lerCalendarioFaseGrupos()) {
            int rodada = converterRodada(campos[0]);
            Grupo grupo = gruposPorIdentificador.get(campos[1].toUpperCase(Locale.ROOT));
            if (grupo == null) {
                throw new IllegalStateException("Grupo do calendário não encontrado: " + campos[1]);
            }

            Time mandante = buscarTimeNoGrupo(grupo, campos[2]);
            Time visitante = buscarTimeNoGrupo(grupo, campos[3]);
            String chaveConfronto = criarChaveConfronto(grupo, mandante, visitante);
            if (!confrontos.add(chaveConfronto)) {
                throw new IllegalStateException("Confronto repetido no calendário: " + campos[2] + " x " + campos[3]);
            }

            String chaveRodada = grupo.getIdentificador() + "-" + rodada;
            int ordemNaRodada = partidasPorRodada.getOrDefault(chaveRodada, 0) + 1;
            partidasPorRodada.put(chaveRodada, ordemNaRodada);

            int quantidadePartidasGrupo = partidasPorGrupo.getOrDefault(grupo.getIdentificador(), 0) + 1;
            partidasPorGrupo.put(grupo.getIdentificador(), quantidadePartidasGrupo);

            String id = "FG-" + grupo.getIdentificador() + "-R" + rodada + "-J" + ordemNaRodada;
            partidas.add(PartidaTorneio.criarFaseDeGrupos(id, rodada, grupo, mandante, visitante));
        }

        validarCalendarioFaseGrupos(partidas, gruposPorIdentificador, partidasPorGrupo, partidasPorRodada);
        return List.copyOf(partidas);
    }

    private Map<String, Grupo> indexarGrupos(List<Grupo> grupos) {
        Objects.requireNonNull(grupos, "A lista de grupos não pode ser nula");
        if (grupos.size() != QUANTIDADE_GRUPOS) {
            throw new IllegalArgumentException("A Copa deve possuir exatamente 12 grupos");
        }

        Map<String, Grupo> gruposPorIdentificador = new HashMap<>();
        for (Grupo grupo : grupos) {
            Objects.requireNonNull(grupo, "Os grupos não podem ser nulos");
            Grupo anterior = gruposPorIdentificador.putIfAbsent(grupo.getIdentificador(), grupo);
            if (anterior != null) {
                throw new IllegalArgumentException("Existem grupos com o mesmo identificador: " + grupo.getIdentificador());
            }
        }

        return gruposPorIdentificador;
    }

    private int converterRodada(String valor) {
        try {
            int rodada = Integer.parseInt(valor);
            if (rodada < 1 || rodada > 3) {
                throw new IllegalStateException("Rodada inválida no calendário: " + valor);
            }
            return rodada;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Rodada inválida no calendário: " + valor, e);
        }
    }

    private Time buscarTimeNoGrupo(Grupo grupo, String nome) {
        String nomeNormalizado = normalizarNome(nome);

        for (Time time : grupo.getTimes()) {
            if (normalizarNome(time.getNome()).equals(nomeNormalizado)) {
                return time;
            }
        }

        throw new IllegalStateException("Seleção " + nome + " não pertence ao grupo " + grupo.getIdentificador());
    }

    private String criarChaveConfronto(Grupo grupo, Time mandante, Time visitante) {
        String primeiro = normalizarNome(mandante.getNome());
        String segundo = normalizarNome(visitante.getNome());

        if (primeiro.compareTo(segundo) > 0) {
            String temporario = primeiro;
            primeiro = segundo;
            segundo = temporario;
        }

        return grupo.getIdentificador() + ":" + primeiro + ":" + segundo;
    }

    private void validarCalendarioFaseGrupos(List<PartidaTorneio> partidas, Map<String, Grupo> grupos, Map<String, Integer> partidasPorGrupo, Map<String, Integer> partidasPorRodada) {
        if (partidas.size() != QUANTIDADE_PARTIDAS_FASE_GRUPOS) {
            throw new IllegalStateException("O calendário deve possuir exatamente 72 partidas");
        }

        for (String identificadorGrupo : grupos.keySet()) {
            if (partidasPorGrupo.getOrDefault(identificadorGrupo, 0) != PARTIDAS_POR_GRUPO) {
                throw new IllegalStateException("O grupo " + identificadorGrupo + " deve possuir seis partidas");
            }

            for (int rodada = 1; rodada <= 3; rodada++) {
                String chaveRodada = identificadorGrupo + "-" + rodada;
                if (partidasPorRodada.getOrDefault(chaveRodada, 0) != PARTIDAS_POR_RODADA) {
                    throw new IllegalStateException("O grupo " + identificadorGrupo + " deve possuir duas partidas na rodada " + rodada);
                }
            }
        }
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
