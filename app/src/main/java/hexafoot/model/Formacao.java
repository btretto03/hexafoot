package hexafoot.model;

/**
 * Enum Formacao - Define a tática do time em campo.
 */
public enum Formacao {

    // --- formações ofensivas ---
    F_4_3_3(1.15, 0.85),
    F_3_4_3(1.10, 0.90),
    F_4_2_4(1.20, 0.80),   

    // --- formações equilibradas ---
    F_4_4_2(1.00, 1.00),
    F_4_2_3_1(1.05, 0.95),
    F_3_5_2(0.95, 1.05),

    // --- formações defensivas ---
    F_5_4_1(0.80, 1.20), 
    F_5_3_2(0.85, 1.15),
    F_4_5_1(0.90, 1.10);

    private final double modificadorAtaque;
    private final double modificadorDefesa;

    Formacao(double modificadorAtaque, double modificadorDefesa) {
        this.modificadorAtaque = modificadorAtaque;
        this.modificadorDefesa = modificadorDefesa;
    }

    public double getModificadorAtaque() {
        return modificadorAtaque;
    }

    public double getModificadorDefesa() {
        return modificadorDefesa;
    }
}