package it.unicam.cs.mpgc.rpg126036.interaction;

import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.StatRequirement;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la ramificazione successo/fallimento di una {@link StatCheckInteraction}.
 */
class StatCheckInteractionTest {

    private Player conCarisma(int valore) {
        Player player = new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
        player.aumentaStatistica(StatType.CARISMA, valore);
        return player;
    }

    private StatCheckInteraction check() {
        return new StatCheckInteraction("check", "Convinci l'NPC",
                new StatRequirement(StatType.CARISMA, 2),
                "Riuscito", 0, 30,
                "Fallito", 10, 5);
    }

    @Test
    void ilRamoDiSuccessoApplicaISuoiEffetti() {
        Player player = conCarisma(2);

        InteractionResult esito = check().esegui(player);

        assertTrue(esito.successo());
        assertEquals("Riuscito", esito.messaggio());
        assertEquals(30, player.getXp());
        assertEquals(Player.ENERGIA_MASSIMA, player.getEnergia());
    }

    @Test
    void ilRamoDiFallimentoApplicaISuoiEffetti() {
        Player player = conCarisma(1);

        InteractionResult esito = check().esegui(player);

        assertFalse(esito.successo());
        assertEquals("Fallito", esito.messaggio());
        assertEquals(5, player.getXp());
        assertEquals(Player.ENERGIA_MASSIMA - 10, player.getEnergia());
    }
}
