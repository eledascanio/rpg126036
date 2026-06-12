package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica che l'autosave salvi la partita alla conclusione di un capitolo.
 */
class AutoSaveListenerTest {

    @Test
    void laConclusioneDiUnCapitoloSalvaLaPartita(@TempDir Path dir) {
        XmlSaveRepository repository = new XmlSaveRepository(dir);
        GameState stato = new GameState(new Player("Eroe", CharacterClass.STUDENTE_MODELLO));
        AutoSaveListener listener = new AutoSaveListener(repository, stato);

        listener.onChapterCompleted(new Chapter("capitolo1", "Capitolo 1"));

        assertTrue(repository.exists("Eroe"));
        assertTrue(repository.load("Eroe").isPresent());
    }
}
