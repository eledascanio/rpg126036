package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Archetipi di personaggio selezionabili. Ogni classe definisce le statistiche
 * iniziali con cui il personaggio comincia la partita.
 *
 * <p>Le tre classi sono bilanciate: ciascuna distribuisce lo stesso totale di
 * punti (18) ma specializzato su una statistica diversa.</p>
 */
public enum CharacterClass {

    /** Specializzato nell'esame delle scene e nella raccolta di indizi. */
    INVESTIGATORE("Investigatore", Map.of(
            StatType.INVESTIGAZIONE, 8,
            StatType.CARISMA, 4,
            StatType.INTUIZIONE, 6)),

    /** Specializzato nelle interazioni sociali e nella persuasione. */
    DIPLOMATICO("Diplomatico", Map.of(
            StatType.INVESTIGAZIONE, 4,
            StatType.CARISMA, 8,
            StatType.INTUIZIONE, 6)),

    /** Specializzato nel cogliere dettagli nascosti e nell'analisi psicologica. */
    PROFILER("Profiler", Map.of(
            StatType.INVESTIGAZIONE, 6,
            StatType.CARISMA, 4,
            StatType.INTUIZIONE, 8));

    private final String nomeVisualizzato;
    private final Map<StatType, Integer> statisticheIniziali;

    CharacterClass(String nomeVisualizzato, Map<StatType, Integer> statisticheIniziali) {
        this.nomeVisualizzato = nomeVisualizzato;
        // Copia difensiva su EnumMap: efficiente e ordinata secondo StatType.
        this.statisticheIniziali = new EnumMap<>(statisticheIniziali);
        // Ogni classe deve definire un valore per ciascuna statistica.
        if (this.statisticheIniziali.size() != StatType.values().length) {
            throw new IllegalArgumentException(
                    "La classe " + name() + " non definisce tutte le statistiche.");
        }
    }

    /**
     * @return il nome della classe adatto alla visualizzazione.
     */
    public String getNomeVisualizzato() {
        return nomeVisualizzato;
    }

    /**
     * Restituisce il valore iniziale della statistica indicata per questa classe.
     *
     * @param tipo la statistica richiesta (non nullo)
     * @return il valore iniziale associato
     */
    public int statisticaIniziale(StatType tipo) {
        return statisticheIniziali.get(tipo);
    }

    /**
     * @return la mappa, in sola lettura, delle statistiche iniziali della classe.
     */
    public Map<StatType, Integer> getStatisticheIniziali() {
        return Collections.unmodifiableMap(statisticheIniziali);
    }
}
