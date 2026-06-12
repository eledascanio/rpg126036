package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la navigazione del grafo di scene di un {@link Chapter}.
 */
class ChapterTest {

    /** Capitolo con inizio -> fine, dove "fine" e' terminale. */
    private Chapter capitoloLineare() {
        Chapter capitolo = new Chapter("c", "Titolo");
        Scene inizio = new Scene("inizio", "Inizio", "...");
        inizio.aggiungiTransizione("Prosegui", "fine");
        capitolo.aggiungiScena(inizio);
        capitolo.aggiungiScena(new Scene("fine", "Fine", "..."));
        return capitolo;
    }

    @Test
    void laPrimaScenaAggiuntaEQuellaIniziale() {
        Chapter capitolo = capitoloLineare();

        assertEquals("inizio", capitolo.getScenaCorrente().getId());
        assertEquals("inizio", capitolo.getIdScenaIniziale().orElseThrow());
    }

    @Test
    void navigareVersoUnaDestinazioneValidaCompletaLaScenaLasciata() {
        Chapter capitolo = capitoloLineare();

        assertTrue(capitolo.vaiA("fine"));
        assertEquals("fine", capitolo.getScenaCorrente().getId());
        assertTrue(capitolo.getScena("inizio").orElseThrow().isCompletata());
    }

    @Test
    void navigareSenzaUnArcoValidoFallisce() {
        Chapter capitolo = capitoloLineare();

        assertFalse(capitolo.vaiA("inesistente"));
        assertEquals("inizio", capitolo.getScenaCorrente().getId());
    }

    @Test
    void ilCapitoloECompletatoQuandoLaScenaCorrenteETerminale() {
        Chapter capitolo = capitoloLineare();

        assertFalse(capitolo.isCompletato());
        capitolo.vaiA("fine");
        assertTrue(capitolo.isCompletato());
    }

    @Test
    void impostareUnaScenaInizialeInesistenteSollevaEccezione() {
        Chapter capitolo = capitoloLineare();

        assertThrows(IllegalArgumentException.class, () -> capitolo.setScenaIniziale("ignota"));
    }
}
