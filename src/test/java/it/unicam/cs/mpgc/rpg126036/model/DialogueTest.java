package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica i dialoghi semplici e condizionati e la loro selezione della battuta.
 */
class DialogueTest {

    private Player conCarisma(int valore) {
        Player player = new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
        player.aumentaStatistica(StatType.CARISMA, valore);
        return player;
    }

    @Test
    void ilDialogoSempliceMostraSempreLaStessaBattuta() {
        Dialogue dialogo = Dialogue.semplice("Ciao");

        assertFalse(dialogo.isCondizionato());
        assertTrue(dialogo.isAccessibile(conCarisma(0)));
        assertEquals("Ciao", dialogo.parlaCon(conCarisma(0)));
    }

    @Test
    void ilDialogoCondizionatoMostraLaBattutaInBaseAllaStatistica() {
        Dialogue dialogo = Dialogue.condizionato(
                new StatRequirement(StatType.CARISMA, 2), "Mi fido di te", "Vattene");

        assertTrue(dialogo.isCondizionato());
        // Sotto soglia: battuta di blocco.
        assertEquals("Vattene", dialogo.parlaCon(conCarisma(1)));
        // Soglia raggiunta: battuta di successo.
        assertEquals("Mi fido di te", dialogo.parlaCon(conCarisma(2)));
    }
}
