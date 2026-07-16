package hexafoot.ui;

import javafx.scene.media.AudioClip;

/** Carrega e reproduz os efeitos sonoros usados pela interface da partida. */
public class TocadorDeSons {
    private final AudioClip somComecoJogo;
    private final AudioClip somFimDeJogo;
    private final AudioClip somGol;
    private final AudioClip somGolAdversario;
    private final AudioClip somCartao;
    private final AudioClip somLesao;

    public TocadorDeSons() {
        this.somComecoJogo = carregarSom("começoJogo.mp3");
        this.somFimDeJogo = carregarSom("acabaJogo.mp3");
        this.somGol = carregarSom("Gol.mp3");
        this.somGolAdversario = carregarSom("golAdversario.mp3");
        this.somCartao = carregarSom("cartao.mp3");
        this.somLesao = carregarSom("Lesao.mp3");
    }

    /**
     * Carrega um áudio empacotado no diretório de recursos da aplicação.
     *
     * @param nomeArquivo nome do arquivo dentro de {@code /data/sons/}
     * @return clipe pronto para reprodução
     */
    private AudioClip carregarSom(String nomeArquivo) {
        String caminho = getClass().getResource("/data/sons/" + nomeArquivo).toExternalForm();
        return new AudioClip(caminho);
    }

    public void tocarComecoJogo() {
        somComecoJogo.play();
    }

    public void tocarFimDeJogo() {
        somFimDeJogo.play();
    }

    public void tocarGol() {
        somGol.play();
    }

    public void tocarGolAdversario() {
        somGolAdversario.play();
    }

    public void tocarCartao() {
        somCartao.play();
    }

    public void tocarLesao() {
        somLesao.play();
    }
}
