package hexafoot.dados;

import hexafoot.model.FaseTorneio;
import hexafoot.model.Grupo;
import hexafoot.model.PartidaTorneio;
import hexafoot.model.Time;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transforma os dados dos CSVs do torneio em objetos do domínio.
 */
public class FabricaTorneio {
    private final LeitorCSVTorneio leitor;

    public FabricaTorneio() {
        this.leitor = new LeitorCSVTorneio();
    }

    public List<Grupo> montarGrupos(List<Time> selecoes) {
        Map<String, Time> selecoesPorNome = indexarSelecoes(selecoes);
        List<Grupo> grupos = new ArrayList<>();
        Set<Time> selecoesAlocadas = new HashSet<>();

        for (String[] campos : leitor.lerGrupos()) {
            List<Time> timesDoGrupo = new ArrayList<>();

            for (int i = 1; i < campos.length; i++) {
                Time time = selecoesPorNome.get(normalizarNome(campos[i]));
                selecoesAlocadas.add(time);
                timesDoGrupo.add(time);
            }

            grupos.add(new Grupo(campos[0], timesDoGrupo));
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
            int rodada = Integer.parseInt(campos[0]);
            Grupo grupo = gruposPorIdentificador.get(campos[1].toUpperCase());

            Time mandante = buscarTimeNoGrupo(grupo, campos[2]);
            Time visitante = buscarTimeNoGrupo(grupo, campos[3]);
            String chaveConfronto = criarChaveConfronto(grupo, mandante, visitante);
            confrontos.add(chaveConfronto);
            String chaveRodada = grupo.getIdentificador() + "-" + rodada;
            int ordemNaRodada = partidasPorRodada.getOrDefault(chaveRodada, 0) + 1;
            partidasPorRodada.put(chaveRodada, ordemNaRodada);

            int quantidadePartidasGrupo = partidasPorGrupo.getOrDefault(grupo.getIdentificador(), 0) + 1;
            partidasPorGrupo.put(grupo.getIdentificador(), quantidadePartidasGrupo);

            String id = "FG-" + grupo.getIdentificador() + "-R" + rodada + "-J" + ordemNaRodada;
            partidas.add(PartidaTorneio.criarFaseDeGrupos(id, rodada, grupo, mandante, visitante));
        }

        return List.copyOf(partidas);
    }

    public List<PartidaTorneio> montarChaveamentoMataMata() {
        List<PartidaTorneio> partidas = new ArrayList<>();
        Set<String> idsPartidas = new HashSet<>();

        for (String[] campos : leitor.lerGabaritoMataMata()) {
            FaseTorneio fase = converterFaseMataMata(campos[0]);
            String id = campos[1];

            idsPartidas.add(id);
            partidas.add(PartidaTorneio.criarEliminatoria(id, fase, campos[2], campos[3]));
        }

        return List.copyOf(partidas);
    }

    private FaseTorneio converterFaseMataMata(String fase) {
        if (fase.equals("DezesseisAvos")) {
            return FaseTorneio.DEZESSEIS_AVOS;
        }
        if (fase.equals("Oitavas")) {
            return FaseTorneio.OITAVAS;
        }
        if (fase.equals("Quartas")) {
            return FaseTorneio.QUARTAS;
        }
        if (fase.equals("Semis")) {
            return FaseTorneio.SEMIFINAL;
        }
        if (fase.equals("TerceiroLugar")) {
            return FaseTorneio.TERCEIRO_LUGAR;
        }
        if (fase.equals("Final")) {
            return FaseTorneio.FINAL;
        }

        throw new IllegalStateException("Fase não existe no chaveamento: " + fase);
    }


    private Map<String, Grupo> indexarGrupos(List<Grupo> grupos) {
        Map<String, Grupo> gruposPorIdentificador = new HashMap<>();
        
        for (Grupo grupo : grupos) {
            gruposPorIdentificador.put(grupo.getIdentificador(), grupo);
        }

        return gruposPorIdentificador;
    }

    private Time buscarTimeNoGrupo(Grupo grupo, String nome) {
        String nomeNormalizado = normalizarNome(nome);

        for (Time time : grupo.getTimes()) {
            if (normalizarNome(time.getNome()).equals(nomeNormalizado)) {
                return time;
            }
        }

        throw new IllegalStateException("Seleção " + nome + " não encontrada no grupo " + grupo.getIdentificador());
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

    private Map<String, Time> indexarSelecoes(List<Time> selecoes) {
        Map<String, Time> selecoesPorNome = new HashMap<>();

        for (Time time : selecoes) {
            String nomeNormalizado = normalizarNome(time.getNome());
            selecoesPorNome.put(nomeNormalizado, time);
        }

        return selecoesPorNome;
    }

    private String normalizarNome(String nome) {
        String semAcentos = Normalizer.normalize(nome.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase().replace(' ', '_');
    }
}
