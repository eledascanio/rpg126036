package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Objects;

/**
 * Arco del grafo di navigazione: collega la scena corrente a una scena
 * destinazione tramite una scelta etichettata.
 *
 * @param etichetta      testo della scelta mostrata al giocatore
 * @param idDestinazione id della scena verso cui conduce la transizione
 */
public record Transition(String etichetta, String idDestinazione) {

    public Transition {
        Objects.requireNonNull(etichetta, "L'etichetta non puo' essere nulla.");
        Objects.requireNonNull(idDestinazione, "L'id di destinazione non puo' essere nullo.");
    }
}
