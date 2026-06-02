package hexafoot.model.strategy;

public class TaticaOfensiva implements EstrategiaSimulacao {
    // Formações ofensivas
    // 4-3-3
    // 3-4-3
    // 4-2-4
    
    @Override
    public double getModificadorAtaque() {
        return 1.1; // Aumenta o ataque em 10%
    }

    @Override
    public double getModificadorDefesa() {
        return 0.9; // Diminui a defesa em 10%
    }

    @Override
    public double getMultiplicadorDesgaste() {
        return 1.1; // Aumenta o desgaste em 10%
    }
}
