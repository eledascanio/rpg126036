package it.unicam.cs.mpgc.rpg126036.view;

import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica il rilevamento delle collisioni del personaggio con i muri della scena.
 */
class MappaCollisioniTest {

    private static final double LATO = 28;

    @Test
    void senzaMuriNonCollideMai() {
        MappaCollisioni collisioni = new MappaCollisioni(new ArrayList<>(), LATO);

        assertFalse(collisioni.collide(0, 0));
        assertFalse(collisioni.collide(500, 300));
    }

    @Test
    void collideQuandoSiSovrapponeAUnMuro() {
        List<Rectangle2D> muri = List.of(new Rectangle2D(100, 100, 50, 50));
        MappaCollisioni collisioni = new MappaCollisioni(muri, LATO);

        // Personaggio (28x28) il cui angolo cade dentro il muro: c'è sovrapposizione.
        assertTrue(collisioni.collide(120, 120));
    }

    @Test
    void nonCollideQuandoELontanoDaiMuri() {
        List<Rectangle2D> muri = List.of(new Rectangle2D(100, 100, 50, 50));
        MappaCollisioni collisioni = new MappaCollisioni(muri, LATO);

        assertFalse(collisioni.collide(300, 300));
    }

    @Test
    void riflettiLeModificheAllaListaCondivisa() {
        // La scena ripopola la stessa lista a ogni cambio: la mappa deve vederne gli effetti.
        List<Rectangle2D> muri = new ArrayList<>();
        MappaCollisioni collisioni = new MappaCollisioni(muri, LATO);
        assertFalse(collisioni.collide(120, 120));

        muri.add(new Rectangle2D(100, 100, 50, 50));
        assertTrue(collisioni.collide(120, 120));
    }
}
