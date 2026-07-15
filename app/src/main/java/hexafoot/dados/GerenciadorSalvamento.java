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
 * Entidade GerenciadorSalvamento, ela é responsável por salvar e carregar o estado do torneio em arquivos.
 */
public class GerenciadorSalvamento {
    private static final String PASTA_SAVES = "saves";
    private static final String NOME_ARQUIVO = "campanha.save";

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

    public String obterDataSalvamento() {
        File arquivo = new File(PASTA_SAVES, NOME_ARQUIVO);
        if (arquivo.exists() == false) {
            return "";
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");
        return formato.format(new Date(arquivo.lastModified()));
    }
}