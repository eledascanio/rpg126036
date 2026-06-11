package it.unicam.cs.mpgc.rpg126036.interaction;

import it.unicam.cs.mpgc.rpg126036.model.Player;

/**
 * Azione interattiva generica che il giocatore puo' compiere nel mondo di gioco
 * (parlare con un NPC, esaminare un oggetto, affrontare un check di statistica,
 * ...). Ogni interazione produce un {@link InteractionResult}.
 */
public interface Interaction {

    /**
     * @return l'identificativo univoco dell'interazione
     */
    String getId();

    /**
     * @return la descrizione/testo di presentazione dell'interazione
     */
    String getDescrizione();

    /**
     * Indica se l'interazione e' attualmente disponibile per il giocatore.
     * Per default e' sempre disponibile.
     *
     * @param giocatore il giocatore (non nullo)
     * @return {@code true} se l'interazione puo' essere eseguita
     */
    default boolean isDisponibile(Player giocatore) {
        return true;
    }

    /**
     * Esegue l'interazione applicandone gli effetti al giocatore.
     *
     * @param giocatore il giocatore che interagisce (non nullo)
     * @return l'esito dell'interazione
     */
    InteractionResult esegui(Player giocatore);
}
