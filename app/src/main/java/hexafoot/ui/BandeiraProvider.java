package hexafoot.ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Provedor de imagens de bandeiras para as seleções do jogo.
 */
public class BandeiraProvider {
    private static final Map<String, Image> cache = new HashMap<>();

    /**
     * Retorna a imagem da bandeira de um país.
     *
     * @param nomePaisBruto identificador do país (ex: "brasil", "alemanha")
     * @return objeto Image da bandeira ou null se não for possível carregar
     */
    public static Image obterImagemBandeira(String nomePaisBruto) {
        if (nomePaisBruto == null) return null;
        String nome = nomePaisBruto.trim().toLowerCase();
        
        if (cache.containsKey(nome)) {
            return cache.get(nome);
        }

        String caminho = "/data/bandeiras/" + nome + ".png";
        InputStream stream = BandeiraProvider.class.getResourceAsStream(caminho);
        if (stream == null) {
            // Tenta fallback para generic.png
            stream = BandeiraProvider.class.getResourceAsStream("/data/bandeiras/generic.png");
        }

        if (stream != null) {
            try {
                Image img = new Image(stream);
                cache.put(nome, img);
                return img;
            } catch (Exception e) {
                System.err.println("Erro ao carregar imagem da bandeira: " + caminho);
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Cria um componente ImageView configurado com a bandeira do país.
     *
     * @param nomePaisBruto identificador do país
     * @param largura largura desejada
     * @param altura altura desejada
     * @return ImageView configurado ou null
     */
    public static ImageView criarImageViewBandeira(String nomePaisBruto, double largura, double altura) {
        Image img = obterImagemBandeira(nomePaisBruto);
        if (img == null) return null;
        ImageView iv = new ImageView(img);
        iv.setFitWidth(largura);
        iv.setFitHeight(altura);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }
}
