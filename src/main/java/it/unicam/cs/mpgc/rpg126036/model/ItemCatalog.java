package it.unicam.cs.mpgc.rpg126036.model;

/**
 * Catalogo degli oggetti raccoglibili del gioco.
 *
 * <p>NOTA: la descrizione della chiave e' provvisoria e personalizzabile.</p>
 */
public final class ItemCatalog {

    /** Identificativo della chiave del primo capitolo, condiviso con l'XML e gli achievement. */
    public static final String ID_CHIAVE_CAPITOLO1 = "chiave_capitolo1";

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
        return new Item(ID_CHIAVE_CAPITOLO1, "Chiave",
                "Una chiave trovata per terra. Per ora non sembra servire a nulla.");
    }
}
