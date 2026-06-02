package hexafoot.model.strategy;
/**
 * Interface que define a estratégia de simulação para o jogo, utilizaresmo o padrao Strategy que vai
 * permitir diferentes táticas (ofensiva, defensiva, equilibrada) que influenciam o resultado da simumulação
 *  com multiplicadores de ataque, defesa e desgaste.
 */

public interface EstrategiaSimulacao {
    // Devolve o multiplicador de ataque (tendo valores maiores para ofensiva e menores para retranca)
    double getModificadorAtaque();

    // devolve o multiplicador de defesa
    double getModificadorDefesa();

    // multiplicador pro cansaço (valores maiores para ofensiva e menores para retranca)
    double getMultiplicadorDesgaste();
    
}
