package it.unicam.cs.mpgc.rpg126036.achievement;

import java.util.List;

/**
 * Catalogo dei traguardi disponibili nel gioco. Fornisce le definizioni degli
 * {@link Achievement} con cui inizializzare l' {@link AchievementManager}.
 *
 * <p>NOTA: nella demo e' presente un solo traguardo; il catalogo e' predisposto
 * per ospitarne altri in futuro.</p>
 */
public final class AchievementCatalog {

    /** Identificativo del traguardo "chiave trovata nel capitolo 1". */
    public static final String ID_CHIAVE_CAPITOLO1 = "chiave_capitolo1_trovata";

    private AchievementCatalog() {
        // Classe di sole utilita' statiche: non istanziabile.
    }

    /**
     * Traguardo sbloccato quando si raccoglie la chiave nel primo capitolo.
     *
     * @return la definizione del traguardo
     */
    public static Achievement chiaveCapitoloUno() {
        return new Achievement(ID_CHIAVE_CAPITOLO1, "Prima chiave",
                "Hai trovato la chiave nascosta nel Capitolo 1.");
    }

    /**
     * @return tutte le definizioni dei traguardi del gioco, in ordine
     */
    public static List<Achievement> tutti() {
        return List.of(chiaveCapitoloUno());
    }
}
