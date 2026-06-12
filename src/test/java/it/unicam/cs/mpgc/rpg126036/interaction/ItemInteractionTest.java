package it.unicam.cs.mpgc.rpg126036.interaction;

import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Item;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica l'esame e la raccolta di un oggetto tramite {@link ItemInteraction}.
 */
class ItemInteractionTest {

    private final Item chiave = new Item("chiave", "Chiave", "Una chiave arrugginita.");

    private Player giocatore() {
        return new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
    }

    @Test
    void esaminareMostraLaDescrizioneSenzaRaccogliere() {
        ItemInteraction interazione = new ItemInteraction(chiave);

        InteractionResult esito = interazione.esamina();

        assertTrue(esito.successo());
        assertTrue(interazione.isPresente());
    }

    @Test
    void raccogliereAggiungeAllInventarioEFaSparireLOggetto() {
        ItemInteraction interazione = new ItemInteraction(chiave);
        Player player = giocatore();

        InteractionResult esito = interazione.raccogli(player);

        assertTrue(esito.successo());
        assertFalse(interazione.isPresente());
        assertTrue(player.getInventario().contiene("chiave"));
    }

    @Test
    void nonSiPuoRaccogliereDueVolteLoStessoOggetto() {
        ItemInteraction interazione = new ItemInteraction(chiave);
        Player player = giocatore();
        interazione.raccogli(player);

        InteractionResult secondo = interazione.raccogli(player);
        assertFalse(secondo.successo());
        // Anche l'esame non trova piu' nulla.
        assertFalse(interazione.esamina().successo());
    }
}
