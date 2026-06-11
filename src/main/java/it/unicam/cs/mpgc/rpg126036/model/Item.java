package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Objects;

/**
 * Oggetto raccoglibile del gioco. Value object immutabile identificato da un id.
 *
 * @param id          identificativo univoco dell'oggetto
 * @param nome        nome visualizzato
 * @param descrizione breve descrizione dell'oggetto
 */
public record Item(String id, String nome, String descrizione) {

    public Item {
        Objects.requireNonNull(id, "L'id non puo' essere nullo.");
        Objects.requireNonNull(nome, "Il nome non puo' essere nullo.");
        Objects.requireNonNull(descrizione, "La descrizione non puo' essere nulla.");
    }
}
