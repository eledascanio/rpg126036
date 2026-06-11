package it.unicam.cs.mpgc.rpg126036.persistence;

import java.util.List;

/**
 * Riferimenti ai contenuti presenti in una scena, espressi per id. La risoluzione
 * degli id nelle istanze concrete (NPC, oggetti, enigmi) spetta a chi assembla il
 * gioco, tramite i cataloghi e le factory.
 *
 * @param npc     id degli NPC presenti nella scena
 * @param oggetti id degli oggetti raccoglibili nella scena
 * @param enigmi  id degli enigmi presenti nella scena
 */
public record SceneContents(List<String> npc, List<String> oggetti, List<String> enigmi) {

    public SceneContents {
        npc = List.copyOf(npc);
        oggetti = List.copyOf(oggetti);
        enigmi = List.copyOf(enigmi);
    }

    /**
     * @return un contenuto vuoto
     */
    public static SceneContents vuoto() {
        return new SceneContents(List.of(), List.of(), List.of());
    }
}
