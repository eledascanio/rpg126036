package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica che l'{@link Npc} deleghi la battuta al proprio {@link Dialogue}.
 */
class NpcTest {

    private Player conCarisma(int valore) {
        Player player = new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
        player.aumentaStatistica(StatType.CARISMA, valore);
        return player;
    }

    @Test
    void unNpcDiContornoParlaSempreAlloStessoModo() {
        Npc npc = new Npc("contorno", "Studente", false, Dialogue.semplice("Che incubo!"));

        assertFalse(npc.isUtile());
        assertEquals("Che incubo!", npc.parla(conCarisma(0)));
    }

    @Test
    void unNpcConDialogoCondizionatoCambiaBattutaConLaStatistica() {
        Npc npc = new Npc("tecnico", "Tecnico", true, Dialogue.condizionato(
                new StatRequirement(StatType.CARISMA, 2), "Ti do la password", "Sono sotto shock"));

        assertTrue(npc.isUtile());
        assertEquals("Sono sotto shock", npc.parla(conCarisma(1)));
        assertEquals("Ti do la password", npc.parla(conCarisma(2)));
    }
}
