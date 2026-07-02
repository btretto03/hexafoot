package hexafoot.model.strategy;

public class TaticaEquilibrada implements EstrategiaSimulacao {
    //tática padrão, sem modificadores
    // Formações equilibradas
    // 4-4-2
    // 4-2-3-1
    // 3-5-2
    
    @Override
    public double getModificadorAtaque() {
        return 1.0; 
    }

    @Override
    public double getModificadorDefesa() {
        return 1.0; 
    }

    @Override
    public double getMultiplicadorDesgaste() {
        return 1.0;
    }
}
