package hexafoot.model.strategy;

import java.io.Serializable;

/**
 * Estratégia que fornece fatores multiplicativos para ataque, defesa e desgaste;
 * o valor {@code 1} representa efeito neutro.
 */

public interface EstrategiaSimulacao extends Serializable {
    // Devolve o multiplicador de ataque (tendo valores maiores para ofensiva e menores para retranca)
    double getModificadorAtaque();

    // devolve o multiplicador de defesa
    double getModificadorDefesa();

    // multiplicador pro cansaço (valores maiores para ofensiva e menores para retranca)
    double getMultiplicadorDesgaste();
    
}
