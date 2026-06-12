package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

/**
 * Archetipi di personaggio selezionabili. Ogni classe definisce le statistiche
 * iniziali con cui il personaggio comincia la partita.
 *
 * <p>Tutte le statistiche partono da 0: la classe assegna un unico bonus di +1
 * alla statistica caratterizzante. La progressione (upgrade di fine capitolo e a
 * ogni 100 XP) fa poi crescere i valori, che restano comunque piccoli e sui quali
 * sono tarate le soglie delle scene (&gt; 0, &ge; 2, &ge; 3).</p>
 */
public enum CharacterClass {

    /** Studente modello: portato all'esame delle scene e alla raccolta di indizi. */
    STUDENTE_MODELLO("Studente modello", Map.of(
            StatType.INVESTIGAZIONE, 1,
            StatType.CARISMA, 0,
            StatType.INTUIZIONE, 0)),

    /** Rappresentante degli studenti: portato alle interazioni sociali e alla persuasione. */
    RAPPRESENTANTE_STUDENTI("Rappresentante degli studenti", Map.of(
            StatType.INVESTIGAZIONE, 0,
            StatType.CARISMA, 1,
            StatType.INTUIZIONE, 0)),

    /** Organizzatore di eventi: portato a cogliere dettagli nascosti e sensazioni. */
    ORGANIZZATORE_EVENTI("Organizzatore di eventi", Map.of(
            StatType.INVESTIGAZIONE, 0,
            StatType.CARISMA, 0,
            StatType.INTUIZIONE, 1));

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

    /**
     * @return la statistica caratterizzante della classe, ossia quella su cui
     *         viene assegnato il bonus iniziale.
     */
    public StatType statisticaPrincipale() {
        return statisticheIniziali.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow();
    }
}
