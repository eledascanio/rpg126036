package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica il comportamento dell'inventario degli oggetti.
 */
class InventoryTest {

    private final Item chiave = new Item("chiave", "Chiave", "Una chiave.");
    private final Item moneta = new Item("moneta", "Moneta", "Una moneta.");

    @Test
    void aggiungeOggettiNuoviERifiutaIDuplicati() {
        Inventory inventario = new Inventory();

        assertTrue(inventario.aggiungi(chiave));
        assertFalse(inventario.aggiungi(chiave));
        assertEquals(1, inventario.size());
    }

    @Test
    void rimuoveEVerificaLaPresenzaPerId() {
        Inventory inventario = new Inventory();
        inventario.aggiungi(chiave);
        inventario.aggiungi(moneta);

        assertTrue(inventario.contiene("moneta"));
        assertTrue(inventario.rimuovi("moneta"));
        assertFalse(inventario.contiene("moneta"));
        assertFalse(inventario.rimuovi("moneta"));
    }

    @Test
    void mantieneLOrdineDiRaccolta() {
        Inventory inventario = new Inventory();
        inventario.aggiungi(chiave);
        inventario.aggiungi(moneta);

        assertEquals(java.util.List.of(chiave, moneta), inventario.getOggetti());
    }
}
