package hexafoot.ui;

import javafx.scene.media.AudioClip;

/**
 * Entidade TocadorDeSons, responsável por gerenciar e reproduzir os efeitos sonoros do jogo.
 */
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