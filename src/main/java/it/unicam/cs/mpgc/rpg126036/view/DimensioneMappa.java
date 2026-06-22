package it.unicam.cs.mpgc.rpg126036.view;

/**
 * Dimensioni in pixel della mappa di gioco: larghezza e altezza. Sono due valori
 * che viaggiano sempre insieme tra la schermata di esplorazione e i suoi
 * collaboratori (disposizione, porte, personaggio, ...): raggrupparli in un unico
 * oggetto evita di passarli ovunque come coppia di parametri.
 *
 * @param larghezza larghezza della mappa in pixel (&gt; 0)
 * @param altezza   altezza della mappa in pixel (&gt; 0)
 */
record DimensioneMappa(double larghezza, double altezza) {

    DimensioneMappa {
        if (larghezza <= 0 || altezza <= 0) {
            throw new IllegalArgumentException("Le dimensioni della mappa devono essere positive.");
        }
    }
}
