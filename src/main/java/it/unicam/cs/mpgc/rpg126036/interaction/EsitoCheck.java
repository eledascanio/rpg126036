package it.unicam.cs.mpgc.rpg126036.interaction;

import java.util.Objects;

/**
 * Esito di un ramo (successo o fallimento) di una {@link StatCheckInteraction}: il
 * testo da mostrare e gli effetti applicati al giocatore (energia spesa e XP
 * guadagnati). Raggruppa i tre dati che descrivono insieme un solo ramo del check,
 * evitando di passarli come terne separate di parametri.
 *
 * @param testo   testo narrativo del ramo
 * @param energia energia consumata dal giocatore (&ge; 0)
 * @param xp      XP guadagnati dal giocatore (&ge; 0)
 */
public record EsitoCheck(String testo, int energia, int xp) {

    public EsitoCheck {
        Objects.requireNonNull(testo, "Il testo non puo' essere nullo.");
        if (energia < 0) {
            throw new IllegalArgumentException("L'energia non puo' essere negativa.");
        }
        if (xp < 0) {
            throw new IllegalArgumentException("Gli XP non possono essere negativi.");
        }
    }
}
