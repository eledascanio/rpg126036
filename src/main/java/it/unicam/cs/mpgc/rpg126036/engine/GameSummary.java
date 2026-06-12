package it.unicam.cs.mpgc.rpg126036.engine;

import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.StatType;

import java.util.Map;
import java.util.Objects;

/**
 * Riepilogo finale della partita, prodotto al completamento del gioco (fine
 * dell'ultimo capitolo) e veicolato da
 * {@link GameListener#onGameCompleted(GameSummary)}. Raccoglie i dati che la
 * schermata conclusiva mostra: identita' del personaggio, statistiche finali e
 * XP totali accumulati.
 *
 * <p>Value object immutabile: la mappa delle statistiche viene copiata in modo
 * difensivo.</p>
 *
 * @param nomePersonaggio   nome del personaggio
 * @param classe            classe del personaggio
 * @param statisticheFinali valore finale di ciascuna statistica
 * @param xpTotali          XP totali accumulati durante la partita
 */
public record GameSummary(String nomePersonaggio, CharacterClass classe,
                          Map<StatType, Integer> statisticheFinali, int xpTotali) {

    public GameSummary {
        Objects.requireNonNull(nomePersonaggio, "Il nome non puo' essere nullo.");
        Objects.requireNonNull(classe, "La classe non puo' essere nulla.");
        statisticheFinali = Map.copyOf(statisticheFinali);
    }
}
