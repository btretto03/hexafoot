package hexafoot.dados;

import hexafoot.service.torneio.GerenciadorTorneio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Persiste o estado do torneio por serialização Java em um arquivo fixo.
 */
public class GerenciadorSalvamento {
    private static final String PASTA_SAVES = "saves";
    private static final String NOME_ARQUIVO = "campanha.save";

    /**
     * Serializa o torneio em {@code saves/campanha.save}, relativo ao diretório de
     * execução. A pasta é criada quando necessário e um arquivo anterior é sobrescrito.
     *
     * @throws IOException se a pasta ou o arquivo não puder ser escrito
     */
    public void salvar(GerenciadorTorneio gerenciadorTorneio) throws IOException {
        File pasta = new File(PASTA_SAVES);
        if (pasta.exists() == false) {
            pasta.mkdirs();
        }

        File arquivo = new File(pasta, NOME_ARQUIVO);
        try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            saida.writeObject(gerenciadorTorneio);
        }
    }

    /**
     * Desserializa o torneio armazenado no arquivo fixo de campanha.
     *
     * @throws IOException se o arquivo não puder ser lido ou estiver corrompido
     * @throws ClassNotFoundException se uma classe do estado salvo não estiver disponível
     */
    public GerenciadorTorneio carregar() throws IOException, ClassNotFoundException {
        File arquivo = new File(PASTA_SAVES, NOME_ARQUIVO);
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (GerenciadorTorneio) entrada.readObject();
        }
    }

    public boolean existeSalvamento() {
        File arquivo = new File(PASTA_SAVES, NOME_ARQUIVO);
        return arquivo.exists();
    }

    /**
     * Formata a última modificação do salvamento no fuso horário padrão da aplicação.
     *
     * @return data no formato {@code dd/MM/yyyy às HH:mm}, ou texto vazio se não
     *         houver salvamento
     */
    public String obterDataSalvamento() {
        File arquivo = new File(PASTA_SAVES, NOME_ARQUIVO);
        if (arquivo.exists() == false) {
            return "";
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");
        return formato.format(new Date(arquivo.lastModified()));
    }
}
