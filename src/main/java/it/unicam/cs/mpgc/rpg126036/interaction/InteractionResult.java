package it.unicam.cs.mpgc.rpg126036.interaction;

import java.util.Objects;

/**
 * Esito di un'{@link Interaction}: indica se l'azione e' riuscita, il messaggio
 * narrativo da mostrare e gli effetti applicati al giocatore (energia spesa e
 * XP guadagnati).
 *
 * @param successo         {@code true} se l'interazione ha avuto esito positivo
 * @param messaggio        testo narrativo dell'esito
 * @param energiaConsumata energia sottratta al giocatore (>= 0)
 * @param xpGuadagnati     XP assegnati al giocatore (>= 0)
 */
public record InteractionResult(boolean successo, String messaggio, int energiaConsumata, int xpGuadagnati) {

    public InteractionResult {
        Objects.requireNonNull(messaggio, "Il messaggio non puo' essere nullo.");
        if (energiaConsumata < 0) {
            throw new IllegalArgumentException("L'energia consumata non puo' essere negativa.");
        }
        if (xpGuadagnati < 0) {
            throw new IllegalArgumentException("Gli XP guadagnati non possono essere negativi.");
        }
    }

    /**
     * Crea un esito senza effetti su energia o XP.
     *
     * @param successo  esito dell'interazione
     * @param messaggio testo narrativo
     * @return l'esito corrispondente
     */
    public static InteractionResult di(boolean successo, String messaggio) {
        return new InteractionResult(successo, messaggio, 0, 0);
    }
}
