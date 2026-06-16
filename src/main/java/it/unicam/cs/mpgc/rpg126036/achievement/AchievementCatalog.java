package it.unicam.cs.mpgc.rpg126036.achievement;

import java.util.List;

/**
 * Catalogo dei traguardi disponibili nel gioco. Fornisce le definizioni degli
 * {@link Achievement} con cui inizializzare l' {@link AchievementManager}.
 *
 * <p>Il catalogo e' predisposto per ospitare nuovi traguardi: per aggiungerne uno
 * basta definirne la factory e includerla in {@link #tutti()}.</p>
 */
public final class AchievementCatalog {

    /** Identificativo del traguardo "chiave trovata nel capitolo 1". */
    public static final String ID_CHIAVE_CAPITOLO1 = "chiave_capitolo1_trovata";

    /** Identificativo del traguardo "PC della vittima trovato al primo click". */
    public static final String ID_PC_PRIMO_COLPO = "pc_vittima_primo_colpo";

    private AchievementCatalog() {
        // Classe di sole utilita' statiche: non istanziabile.
    }

    /**
     * Traguardo sbloccato quando si raccoglie la chiave nel primo capitolo.
     *
     * @return la definizione del traguardo
     */
    public static Achievement chiaveCapitoloUno() {
        return new Achievement(ID_CHIAVE_CAPITOLO1, "Prima prova",
                "Hai trovato la chiave nascosta nel Capitolo 1.");
    }

    /**
     * Traguardo sbloccato individuando il PC di Antonio al primissimo click,
     * senza esaminare nessuno dei cinque computer sbagliati.
     *
     * @return la definizione del traguardo
     */
    public static Achievement pcVittimaAlPrimoColpo() {
        return new Achievement(ID_PC_PRIMO_COLPO, "Cercatore d'oro",
                "Trova il PC di Antonio al primissimo click, senza toccare nessuno dei 5 computer sbagliati.");
    }

    /**
     * @return tutte le definizioni dei traguardi del gioco, in ordine
     */
    public static List<Achievement> tutti() {
        return List.of(chiaveCapitoloUno(), pcVittimaAlPrimoColpo());
    }
}
