package it.unicam.cs.mpgc.rpg126036.engine;

import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifica il costruttore del {@link GameEngine} con indice di capitolo iniziale,
 * usato per riprendere una partita salvata.
 */
class GameEngineIndiceTest {

    private Chapter capitolo(String id) {
        Chapter capitolo = new Chapter(id, "Titolo " + id);
        capitolo.aggiungiScena(new Scene("scena_" + id, "Scena", "Descrizione"));
        return capitolo;
    }

    private GameState statoVuoto() {
        return new GameState(new Player("Eroe", CharacterClass.STUDENTE_MODELLO));
    }

    @Test
    void parteDalCapitoloAllIndiceIndicato() {
        List<Chapter> capitoli = List.of(capitolo("c1"), capitolo("c2"), capitolo("c3"));

        GameEngine engine = new GameEngine(statoVuoto(), capitoli, 2);

        assertEquals("c3", engine.getCapitoloCorrente().getId());
    }

    @Test
    void unIndiceFuoriRangeVieneRifiutato() {
        List<Chapter> capitoli = List.of(capitolo("c1"));

        assertThrows(IllegalArgumentException.class,
                () -> new GameEngine(statoVuoto(), capitoli, 3));
    }
}
