package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Objects;

/**
 * Requisito su una statistica del personaggio: il valore corrente della
 * statistica indicata deve raggiungere o superare una soglia minima.
 *
 * @param tipo         la statistica richiesta
 * @param valoreMinimo la soglia minima da raggiungere
 */
public record StatRequirement(StatType tipo, int valoreMinimo) {

    public StatRequirement {
        Objects.requireNonNull(tipo, "Il tipo di statistica non puo' essere nullo.");
    }

    /**
     * Verifica se il personaggio soddisfa il requisito.
     *
     * @param player il personaggio da valutare (non nullo)
     * @return {@code true} se la statistica del personaggio raggiunge la soglia
     */
    public boolean isSoddisfatto(Player player) {
        Objects.requireNonNull(player, "Il player non puo' essere nullo.");
        return player.getStatistica(tipo) >= valoreMinimo;
    }
}
