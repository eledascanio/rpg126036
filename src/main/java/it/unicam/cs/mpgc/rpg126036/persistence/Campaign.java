package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.model.Chapter;

import java.util.List;
import java.util.Objects;

/**
 * Campagna del gioco: la sequenza ordinata dei capitoli caricati, ciascuno con il
 * proprio grafo di scene e i riferimenti ai contenuti.
 *
 * <p>Aggrega le {@link ChapterDefinition} prodotte dal caricamento e offre due
 * viste: la lista dei {@link Chapter} (con cui costruire il motore) e i contenuti
 * referenziati per scena, da risolvere in fase di presentazione. I contenuti sono
 * indicizzati per (capitolo, scena): id di scena uguali possono ricorrere in
 * capitoli diversi (ad es. "cortile" nel capitolo 1 e nel capitolo 3).</p>
 */
public class Campaign {

    private final List<ChapterDefinition> definizioni;

    /**
     * @param definizioni le definizioni dei capitoli, in ordine (non nulle, non vuote)
     */
    public Campaign(List<ChapterDefinition> definizioni) {
        Objects.requireNonNull(definizioni, "Le definizioni non possono essere nulle.");
        if (definizioni.isEmpty()) {
            throw new IllegalArgumentException("La campagna deve avere almeno un capitolo.");
        }
        this.definizioni = List.copyOf(definizioni);
    }

    /**
     * @return i capitoli della campagna, nell'ordine di gioco
     */
    public List<Chapter> getCapitoli() {
        return definizioni.stream().map(ChapterDefinition::capitolo).toList();
    }

    /**
     * Restituisce i contenuti referenziati da una scena di un capitolo.
     *
     * @param idCapitolo id del capitolo
     * @param idScena    id della scena
     * @return i contenuti della scena, o un contenuto vuoto se capitolo o scena non esistono
     */
    public SceneContents contenutiDi(String idCapitolo, String idScena) {
        return definizioni.stream()
                .filter(def -> def.capitolo().getId().equals(idCapitolo))
                .findFirst()
                .map(def -> def.contenutiDi(idScena))
                .orElse(SceneContents.vuoto());
    }

    /**
     * @return il numero di capitoli della campagna
     */
    public int size() {
        return definizioni.size();
    }
}
