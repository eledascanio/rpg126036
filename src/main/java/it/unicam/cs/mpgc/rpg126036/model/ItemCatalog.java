package it.unicam.cs.mpgc.rpg126036.model;

/**
 * Catalogo degli oggetti raccoglibili del gioco.
 *
 * <p>NOTA: la descrizione della chiave e' provvisoria e personalizzabile.</p>
 */
public final class ItemCatalog {

    private ItemCatalog() {
        // Classe di sole utilita' statiche: non istanziabile.
    }

    /**
     * Chiave raccoglibile nel primo capitolo. Nella demo viene raccolta ma non
     * ha ancora un utilizzo: serve a mostrare la meccanica predisposta per il futuro.
     *
     * @return l'oggetto chiave del capitolo 1
     */
    public static Item chiaveCapitoloUno() {
        return new Item("chiave_capitolo1", "Chiave",
                "Una chiave trovata per terra. Per ora non sembra servire a nulla.");
    }
}
