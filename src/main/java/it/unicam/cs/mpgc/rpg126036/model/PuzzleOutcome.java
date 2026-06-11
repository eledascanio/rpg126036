package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Objects;

/**
 * Esito di un'interazione con un {@link Puzzle}: indica se l'enigma e' stato
 * risolto, l'energia eventualmente consumata, gli XP guadagnati e il messaggio
 * narrativo da mostrare al giocatore.
 *
 * @param risolto          {@code true} se a seguito dell'azione l'enigma risulta risolto
 * @param energiaConsumata energia sottratta al giocatore (>= 0)
 * @param xpGuadagnati     XP assegnati al giocatore (>= 0)
 * @param messaggio        testo narrativo dell'esito
 */
public record PuzzleOutcome(boolean risolto, int energiaConsumata, int xpGuadagnati, String messaggio) {

    public PuzzleOutcome {
        Objects.requireNonNull(messaggio, "Il messaggio non puo' essere nullo.");
        if (energiaConsumata < 0) {
            throw new IllegalArgumentException("L'energia consumata non puo' essere negativa.");
        }
        if (xpGuadagnati < 0) {
            throw new IllegalArgumentException("Gli XP guadagnati non possono essere negativi.");
        }
    }
}
