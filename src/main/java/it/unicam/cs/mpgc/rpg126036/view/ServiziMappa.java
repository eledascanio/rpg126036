package it.unicam.cs.mpgc.rpg126036.view;

import javafx.animation.Timeline;

/**
 * Ruolo dei servizi di mappa della scena: registrazione e rimozione degli
 * elementi interattivi e delle animazioni che li animano.
 *
 * <p>Uno dei ruoli in cui è segregata la {@link RegiaEsplorazione} (ISP). Vi
 * dipendono i componenti di trama che popolano la mappa ({@link Porte},
 * {@link AulaB}, {@link PcVittima}); chi non manipola elementi non vi dipende.</p>
 */
public interface ServiziMappa {

    /** Registra un elemento interattivo sulla mappa, mostrandone nodo ed etichetta. */
    void registra(ElementoScena elemento);

    /** Rimuove un elemento dalla mappa (e dall'eventuale suggerimento di prossimità). */
    void rimuoviElemento(ElementoScena elemento);

    /** Registra un'animazione di scena, da fermare alla ricostruzione della stessa. */
    void registraAnimazione(Timeline animazione);
}
