package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Objects;

/**
 * Indizio raccolto durante l'indagine. A differenza di un {@link Item}, non e'
 * un oggetto fisico ma un'informazione (un nome, una password, un indirizzo, un
 * dettaglio): un "flag di conoscenza" che alimenta il diario del giocatore e puo'
 * sbloccare progressi narrativi.
 *
 * <p>Value object immutabile identificato da un id.</p>
 *
 * @param id     identificativo univoco dell'indizio
 * @param titolo titolo breve mostrato nel diario
 * @param testo  contenuto informativo dell'indizio
 */
public record Clue(String id, String titolo, String testo) {

    public Clue {
        Objects.requireNonNull(id, "L'id non puo' essere nullo.");
        Objects.requireNonNull(titolo, "Il titolo non puo' essere nullo.");
        Objects.requireNonNull(testo, "Il testo non puo' essere nullo.");
    }
}
