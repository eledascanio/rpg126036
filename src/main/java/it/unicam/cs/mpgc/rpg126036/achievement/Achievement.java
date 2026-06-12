package it.unicam.cs.mpgc.rpg126036.achievement;

import java.util.Objects;

/**
 * Traguardo sbloccabile dal giocatore. Value object immutabile identificato da
 * un id: rappresenta la sola definizione del traguardo, mentre lo stato di
 * sblocco e' tracciato dall' {@link AchievementManager}.
 *
 * @param id          identificativo univoco del traguardo
 * @param titolo      titolo visualizzato
 * @param descrizione breve descrizione di come si ottiene
 */
public record Achievement(String id, String titolo, String descrizione) {

    public Achievement {
        Objects.requireNonNull(id, "L'id non puo' essere nullo.");
        Objects.requireNonNull(titolo, "Il titolo non puo' essere nullo.");
        Objects.requireNonNull(descrizione, "La descrizione non puo' essere nulla.");
    }
}
