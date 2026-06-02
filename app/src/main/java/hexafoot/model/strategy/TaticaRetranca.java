package hexafoot.model.strategy;

public class TaticaRetranca implements EstrategiaSimulacao {
    // Formações defensivas
    // 5-4-1
    // 5-3-2
    // 4-5-1

    @Override
    public double getModificadorAtaque() {
        return 0.90;
    }

    @Override
    public double getModificadorDefesa() {
        return 1.10; // Defesa aumenta 10%
    }

    @Override
    public double getMultiplicadorDesgaste() {
        return 0.90; // Desgaste diminui 10%
    }
}
