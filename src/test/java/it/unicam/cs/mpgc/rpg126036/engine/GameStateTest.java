package it.unicam.cs.mpgc.rpg126036.engine;

import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica i flag di progresso e le viste esposte dal {@link GameState}.
 */
class GameStateTest {

    private GameState stato() {
        return new GameState(new Player("Eroe", CharacterClass.STUDENTE_MODELLO));
    }

    @Test
    void iFlagDiProgressoSiAttivanoESiRimuovono() {
        GameState stato = stato();

        assertFalse(stato.hasFlag("porta_aperta"));
        stato.setFlag("porta_aperta");
        assertTrue(stato.hasFlag("porta_aperta"));
        assertTrue(stato.getFlag().contains("porta_aperta"));
        assertTrue(stato.rimuoviFlag("porta_aperta"));
        assertFalse(stato.hasFlag("porta_aperta"));
    }

    @Test
    void inventarioEDiarioSonoQuelliDelGiocatore() {
        GameState stato = stato();

        assertSame(stato.getPlayer().getInventario(), stato.getInventario());
        assertSame(stato.getPlayer().getDiario(), stato.getDiario());
    }

    @Test
    void ilCapitoloCorrenteEsponeLaScenaCorrente() {
        GameState stato = stato();
        Chapter capitolo = new Chapter("c", "Titolo");
        capitolo.aggiungiScena(new Scene("scena", "Scena", "..."));

        stato.setCapitoloCorrente(capitolo);

        assertEquals("c", stato.getCapitoloCorrente().orElseThrow().getId());
        assertEquals("scena", stato.getScenaCorrente().orElseThrow().getId());
    }
}
