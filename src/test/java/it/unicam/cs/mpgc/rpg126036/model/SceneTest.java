package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica gli archi e i flag di stato di una {@link Scene}.
 */
class SceneTest {

    @Test
    void unaScenaSenzaArchiETerminale() {
        Scene scena = new Scene("s", "Scena", "...");

        assertTrue(scena.isTerminale());
        assertFalse(scena.isCompletata());
    }

    @Test
    void aggiungereUnArcoRendeLaScenaNonTerminale() {
        Scene scena = new Scene("s", "Scena", "...");
        scena.aggiungiTransizione("Vai", "altra");

        assertFalse(scena.isTerminale());
        assertEquals(1, scena.getTransizioni().size());
        assertEquals("altra", scena.getTransizioni().get(0).idDestinazione());
    }

    @Test
    void segnaCompletataAggiornaIlFlag() {
        Scene scena = new Scene("s", "Scena", "...");

        scena.segnaCompletata();
        assertTrue(scena.isCompletata());
    }
}
