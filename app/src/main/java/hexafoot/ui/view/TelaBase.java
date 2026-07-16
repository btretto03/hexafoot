package hexafoot.ui.view;

import hexafoot.ui.GameNavigator;

/** Base das telas que precisam acessar a navegação e a sessão compartilhada. */
public abstract class TelaBase implements ScreenView {
    protected final GameNavigator navigator;

    protected TelaBase(GameNavigator navigator) {
        this.navigator = navigator;
    }
}
