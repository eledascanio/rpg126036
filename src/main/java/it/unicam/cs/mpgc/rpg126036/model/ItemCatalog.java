package it.unicam.cs.mpgc.rpg126036.model;

/**
 * Catalogo degli oggetti raccoglibili del gioco.
 */
public final class ItemCatalog {

    /** Identificativo della chiave del primo capitolo, condiviso con l'XML e gli achievement. */
    public static final String ID_CHIAVE_CAPITOLO1 = "chiave_capitolo1";

    private ItemCatalog() {
        // Classe di sole utilita' statiche: non istanziabile.
    }

    /**
     * Chiave raccoglibile nel primo capitolo: una chiave insanguinata trovata fuori
     * dal Polo, che il giocatore riconosce essere di Antonio. Nome e descrizione qui
     * sono quelli registrati nel diario (sezione "Prove"); al momento del ritrovamento
     * la vista la presenta ancora come anonima "chiave insanguinata".
     *
     * @return l'oggetto chiave del capitolo 1
     */
    public static Item chiaveCapitoloUno() {
        return new Item(ID_CHIAVE_CAPITOLO1, "Chiavi di Antonio",
                "Chiavi insanguinate trovate all'esterno del Polo.");
    }
}
