package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la valutazione di un {@link StatRequirement} sul giocatore.
 */
class StatRequirementTest {

    private Player conIntuizione(int valore) {
        Player player = new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
        player.aumentaStatistica(StatType.INTUIZIONE, valore);
        return player;
    }

    @Test
    void eSoddisfattoQuandoLaStatisticaRaggiungeLaSoglia() {
        StatRequirement requisito = new StatRequirement(StatType.INTUIZIONE, 2);

        assertFalse(requisito.isSoddisfatto(conIntuizione(1)));
        assertTrue(requisito.isSoddisfatto(conIntuizione(2)));
        assertTrue(requisito.isSoddisfatto(conIntuizione(3)));
    }
}
