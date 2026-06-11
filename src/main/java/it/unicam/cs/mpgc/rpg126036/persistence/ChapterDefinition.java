package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.model.Chapter;

import java.util.Map;

/**
 * Definizione di un capitolo caricata da file: il {@link Chapter} con il suo
 * grafo di scene e i riferimenti ai contenuti per ciascuna scena.
 *
 * @param capitolo          il capitolo con scene e transizioni
 * @param contenutiPerScena mappa id-scena -> contenuti referenziati
 */
public record ChapterDefinition(Chapter capitolo, Map<String, SceneContents> contenutiPerScena) {

    public ChapterDefinition {
        contenutiPerScena = Map.copyOf(contenutiPerScena);
    }

    /**
     * @param idScena id della scena
     * @return i contenuti della scena, o un contenuto vuoto se assente
     */
    public SceneContents contenutiDi(String idScena) {
        return contenutiPerScena.getOrDefault(idScena, SceneContents.vuoto());
    }
}
