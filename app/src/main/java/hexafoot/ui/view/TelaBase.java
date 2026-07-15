package hexafoot.ui.view;

import hexafoot.ui.GameNavigator;

/**
 * Classe abstrata que padroniza o comportamento comum das telas do jogo.
 */
public abstract class TelaBase implements ScreenView {
    protected final GameNavigator navigator;

    protected TelaBase(GameNavigator navigator) {
        this.navigator = navigator;
    }
}
